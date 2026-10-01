package com.cosmicpve.combat.empowerment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.enchantment.DodgeProcResolver;
import com.cosmicpve.combat.enchantment.EpidemicCarrierBehavior;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcActivation;
import com.cosmicpve.combat.proc.ProcActivationListener;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.equipment.mask.MaskResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Shared one-use, post-commit consumption seam for Jester and Epidemic Carrier. */
public final class NextHitEmpowermentService implements OutgoingDamageContributor,
        ProcCandidateResolver, ProcActivationListener {
    public static final Identifier JESTER = CosmicPVE.id("jester_mask");
    public static final Identifier EPIDEMIC = ModEnchantments.EPIDEMIC_CARRIER.identifier();
    public static final int JESTER_TICKS = 60;
    private final MaskResolver masks;
    private final Map<UUID, Map<Identifier, Charge>> charges = new HashMap<>();
    private final Map<Long, Pending> pending = new HashMap<>();

    public NextHitEmpowermentService(MaskResolver masks) { this.masks = masks; }

    public void armEpidemic(LivingEntity wearer, int level) {
        if (level <= 0) return;
        put(wearer.getUUID(), EPIDEMIC, new Charge(
                EpidemicCarrierBehavior.bonus(level), EpidemicCarrierBehavior.healFraction(level), 0));
    }

    @Override public void activated(ProcActivation activation) {
        if (!activation.candidate().effectId().equals(DodgeProcResolver.CANDIDATE)
                || !(activation.event().target() instanceof Player wearer)
                || masks.resolve(wearer).stream().noneMatch(mask -> mask.behavior() == MaskBehavior.JESTER)) return;
        put(wearer.getUUID(), JESTER, new Charge(.12, 0, activation.event().serverTick() + JESTER_TICKS));
    }

    private void put(UUID wearer, Identifier source, Charge charge) {
        charges.computeIfAbsent(wearer, ignored -> new HashMap<>()).put(source, charge);
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (!ordinaryParent(context) || context.attacker() == null || context.target() == null) return List.of();
        var server = context.attacker().level().getServer();
        if (server == null) return List.of();
        var active = charges.get(context.attacker().getUUID());
        if (active == null) return List.of();
        long tick = server.getTickCount();
        active.entrySet().removeIf(entry -> entry.getValue().expiresAt() > 0 && tick >= entry.getValue().expiresAt());
        if (active.isEmpty()) { charges.remove(context.attacker().getUUID()); return List.of(); }
        var snapshot = Map.copyOf(active);
        if (pending.size() > 1024) pending.clear();
        pending.put(context.attackSequenceId(), new Pending(context.attacker().getUUID(), snapshot));
        return snapshot.entrySet().stream().map(entry -> new OutgoingDamageContribution(
                entry.getKey(), entry.getValue().bonus())).toList();
    }

    private static boolean ordinaryParent(CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY && context.parentSequenceId().isEmpty()
                && (context.category() == AttackCategory.MELEE || context.category() == AttackCategory.PROJECTILE);
    }

    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_VALID_HIT || event.parentSequenceId().isPresent()
                || event.attacker() == null || event.combatResult().isEmpty()
                || !event.combatResult().orElseThrow().isCommittedDamagingHit()
                || !ordinaryParent(event.combatResult().orElseThrow().context())) return List.of();
        Pending entry = pending.remove(event.sequenceId());
        if (entry == null || !entry.wearer().equals(event.attacker().getUUID())) return List.of();
        var result = new ArrayList<ProcCandidate>();
        for (var charge : entry.charges().entrySet()) {
            Identifier source = charge.getKey();
            Charge snapshot = charge.getValue();
            result.add(new ProcCandidate(source, ProcHook.ON_VALID_HIT, 1.0,
                    Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                    Optional.empty(), ChildProcEligibility.ROOT_ONLY, Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION),
                    source, activation -> {
                        var current = charges.get(entry.wearer());
                        if (current == null || !current.remove(source, snapshot)) return;
                        if (current.isEmpty()) charges.remove(entry.wearer());
                        if (snapshot.healFraction() > 0 && activation.event().attacker() != null)
                            activation.event().attacker().heal((float) (snapshot.healFraction()
                                    * activation.event().combatResult().orElseThrow().committedHealthDamage()));
                    }, new ProcProvenance(ProcSourceKind.OTHER, source)));
        }
        return List.copyOf(result);
    }

    public void onDeath(LivingDeathEvent event) {
        if (!event.isCanceled()) clear(event.getEntity().getUUID());
    }
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { clear(event.getEntity().getUUID()); }
    private void clear(UUID wearer) {
        charges.remove(wearer);
        pending.entrySet().removeIf(entry -> entry.getValue().wearer().equals(wearer));
    }

    public boolean hasCharge(UUID wearer, Identifier source) {
        return charges.getOrDefault(wearer, Map.of()).containsKey(source);
    }
    private record Charge(double bonus, double healFraction, long expiresAt) {}
    private record Pending(UUID wearer, Map<Identifier, Charge> charges) {}
}
