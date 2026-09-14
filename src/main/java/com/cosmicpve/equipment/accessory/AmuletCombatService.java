package com.cosmicpve.equipment.accessory;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.enchantment.BleedBehavior;
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
import com.cosmicpve.combat.stack.BleedRuntimeService;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.effect.MobEffects;

/** One integration point for all currently implemented Amulet combat effects. */
public final class AmuletCombatService implements OutgoingDamageContributor, ProcCandidateResolver, ProcActivationListener {
    public static final double BLOOD_DIAMOND_PER_STACK = 0.01;
    public static final double ICICLE_CHANCE = 0.10;
    public static final double BLACK_HEART_BONUS = 0.05;
    public static final net.minecraft.resources.Identifier ICICLE_PROC = CosmicPVE.id("icicle_amulet");
    private final AccessoryResolver accessories;
    private final CombatStackService stacks;
    private final BleedRuntimeService bleedRuntime;
    private final BlackHeartStateService blackHeart;
    public AmuletCombatService(AccessoryResolver accessories, CombatStackService stacks,
            BleedRuntimeService bleedRuntime, BlackHeartStateService blackHeart) {
        this.accessories = accessories; this.stacks = stacks; this.bleedRuntime = bleedRuntime; this.blackHeart = blackHeart;
    }
    public static double bloodDiamondBonus(int attackerBleed, int targetBleed) {
        if (attackerBleed < 0 || targetBleed < 0) throw new IllegalArgumentException("Bleed counts cannot be negative");
        return (attackerBleed + targetBleed) * BLOOD_DIAMOND_PER_STACK;
    }
    @Override public List<OutgoingDamageContribution> resolve(com.cosmicpve.combat.api.CombatContext context) {
        if (!ordinaryRoot(context) || context.attacker() == null || context.target() == null) return List.of();
        long tick = context.attacker().level().getServer() == null ? 0L : context.attacker().level().getServer().getTickCount();
        var result = new java.util.ArrayList<OutgoingDamageContribution>();
        if (accessories.hasAmulet(context.attacker(), AmuletDefinition.BLOOD_DIAMOND)) {
            int count = stacks.count(context.attacker(), BleedBehavior.STACK_ID, tick)
                    + stacks.count(context.target(), BleedBehavior.STACK_ID, tick);
            if (count > 0) result.add(new OutgoingDamageContribution(AmuletDefinition.BLOOD_DIAMOND.id(),
                    count * BLOOD_DIAMOND_PER_STACK));
        }
        if (accessories.hasAmulet(context.attacker(), AmuletDefinition.BLACK_HEART)
                && blackHeart.active(context.attacker(), tick))
            result.add(new OutgoingDamageContribution(AmuletDefinition.BLACK_HEART.id(), BLACK_HEART_BONUS));
        return List.copyOf(result);
    }
    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_VALID_HIT || event.attacker() == null || event.target() == null
                || !accessories.hasAmulet(event.attacker(), AmuletDefinition.ICICLE)
                || !event.target().hasEffect(MobEffects.SLOWNESS)
                || event.combatResult().map(result -> !ordinaryRoot(result.context())).orElse(true)) return List.of();
        return List.of(new ProcCandidate(ICICLE_PROC, ProcHook.ON_VALID_HIT, ICICLE_CHANCE, Optional.empty(), 0L,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.of(ICICLE_PROC),
                ChildProcEligibility.ROOT_ONLY, Set.of(ProcEngine.UNMODIFIED_CHANCE_CLASSIFICATION), ICICLE_PROC,
                activation -> {
                    var target = activation.event().target(); var attacker = activation.event().attacker();
                    if (target == null || attacker == null || target.isDeadOrDying()) return;
                    var added = stacks.addStack(target, BleedBehavior.STACK_ID, 1,
                            StackApplication.ephemeral(Optional.of(attacker.getUUID()), activation.event().tracePlayerId()),
                            activation.event().serverTick());
                    bleedRuntime.reconcileMovement(target, added.finalCount());
                    if (added.added() > 0 || added.refreshed() > 0) activation.markAffected(target);
                }, new ProcProvenance(ProcSourceKind.ACCESSORY, AmuletDefinition.ICICLE.id())));
    }
    @Override public void activated(ProcActivation activation) {
        var tier = CosmicEnchantmentSpecs.find(activation.candidate().effectId()).map(value -> value.tier()).orElse(null);
        if (tier != CosmicEnchantmentTier.MASTERY && tier != CosmicEnchantmentTier.HEROIC) return;
        for (var entity : activation.affectedEntities()) {
            if (!entity.getUUID().equals(activation.event().ownerId())
                    && accessories.hasAmulet(entity, AmuletDefinition.BLACK_HEART))
                blackHeart.activate(entity, activation.event().serverTick());
        }
    }
    private static boolean ordinaryRoot(com.cosmicpve.combat.api.CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY && context.parentSequenceId().isEmpty();
    }
}
