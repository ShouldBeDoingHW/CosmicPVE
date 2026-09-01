package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.registry.ModEnchantments;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.projectile.Projectile;

/** Launch-snapshotted Heroic headshots plus a committed-hit-consumed melee follow-up. */
public final class LethalSniperBehavior implements OutgoingDamageContributor, ProcCandidateResolver {
    public static final double HEADSHOT_FRACTION = .75;
    public static final double PROJECTILE_BONUS_PER_LEVEL = .08;
    public static final double MELEE_BONUS = .10;
    public static final int MELEE_BUFF_TICKS = 200;
    private final ProjectileImpactContextService impacts;
    private final Set<Long> pendingHeadshots = new HashSet<>();
    private final Set<Long> pendingMeleeConsumption = new HashSet<>();
    private final Map<UUID, Long> meleeBuffExpiry = new HashMap<>();

    public LethalSniperBehavior(ProjectileImpactContextService impacts) { this.impacts = impacts; }
    public static double projectileBonus(int level) { return PROJECTILE_BONUS_PER_LEVEL * Math.max(0, Math.min(5, level)); }
    public boolean hasMeleeBuff(UUID entity, long tick) { return meleeBuffExpiry.getOrDefault(entity, -1L) > tick; }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null)
            return List.of();
        if (context.category() == AttackCategory.PROJECTILE && context.directSource() instanceof Projectile projectile) {
            int level = context.effectiveEnchantments().level(ModEnchantments.LETHAL_SNIPER.identifier());
            if (level <= 0 || !SniperBehavior.eligibleWeapon(context.weaponSnapshot().stack())) return List.of();
            boolean headshot = impacts.consume(projectile, context.target()).map(point ->
                    SniperBehavior.isHeadshotAtFraction(context.target().getBoundingBox(), point, HEADSHOT_FRACTION)).orElse(false);
            if (!headshot) return List.of();
            boundedAdd(pendingHeadshots, context.attackSequenceId());
            return List.of(new OutgoingDamageContribution(ModEnchantments.LETHAL_SNIPER.identifier(), projectileBonus(level)));
        }
        if (context.category() == AttackCategory.MELEE && hasMeleeBuff(context.attacker().getUUID(), serverTick(context))) {
            boundedAdd(pendingMeleeConsumption, context.attackSequenceId());
            return List.of(new OutgoingDamageContribution(ModEnchantments.LETHAL_SNIPER.identifier(), MELEE_BONUS));
        }
        return List.of();
    }

    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() == ProcHook.ON_PROJECTILE_HIT && pendingHeadshots.remove(event.sequenceId())
                && event.attacker() != null) {
            return List.of(deterministic(event, activation -> meleeBuffExpiry.put(
                    activation.event().attacker().getUUID(), activation.event().serverTick() + MELEE_BUFF_TICKS)));
        }
        if (event.hook() == ProcHook.ON_VALID_HIT && pendingMeleeConsumption.remove(event.sequenceId())
                && event.attacker() != null) {
            return List.of(deterministic(event, activation -> meleeBuffExpiry.remove(
                    activation.event().attacker().getUUID())));
        }
        return List.of();
    }
    private static long serverTick(CombatContext context) {
        return context.attacker().level().getServer() == null ? 0L : context.attacker().level().getServer().getTickCount();
    }
    private static void boundedAdd(Set<Long> values, long value) {
        if (values.size() > 512) values.clear();
        values.add(value);
    }
    private static ProcCandidate deterministic(ProcEvent event, com.cosmicpve.combat.proc.ProcAction action) {
        return new ProcCandidate(ModEnchantments.LETHAL_SNIPER.identifier(), event.hook(), 1.0,
                Optional.empty(), 0L, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.empty(), ChildProcEligibility.ROOT_ONLY, Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION),
                ModEnchantments.LETHAL_SNIPER.identifier(), action,
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.LETHAL_SNIPER.identifier()));
    }
}
