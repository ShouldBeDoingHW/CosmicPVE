package com.cosmicpve.equipment.skin;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.combat.stack.CombatStackService;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

/** Composes implemented skin behaviors into the existing combat seams. */
public final class WeaponSkinCombatResolver
        implements OutgoingDamageContributor, IncomingDamageContributor, ProcCandidateResolver {
    public static final double MAUI_OUTGOING = 0.04;
    public static final double MAUI_STEAL_CHANCE = 0.10;
    public static final double STORM_CHANCE = 0.03;
    public static final double STORM_TRUE_DAMAGE = 2.0;
    public static final int STORM_SLOWNESS_TICKS = 30;
    public static final int STORM_SLOWNESS_AMPLIFIER = 1;
    public static final double STORM_INCOMING_MULTIPLIER = 0.98;
    public static final double SEASONS_OUTGOING = 0.10;
    public static final double SEASONS_INCOMING_MULTIPLIER = 0.95;

    private final WeaponSkinResolver skins;
    private final CombatStackService stacks;
    private final ChildCombatActionService childActions;

    public WeaponSkinCombatResolver(
            WeaponSkinResolver skins, CombatStackService stacks, ChildCombatActionService childActions) {
        this.skins = skins;
        this.stacks = stacks;
        this.childActions = childActions;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(com.cosmicpve.combat.api.CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.category() != AttackCategory.MELEE) return List.of();
        return skins.resolve(context.weaponSnapshot().stack()).map(definition -> {
            if (definition.id().equals(WeaponSkinDefinitions.MAUIS_HOOK))
                return List.of(new OutgoingDamageContribution(definition.id(), MAUI_OUTGOING));
            if (definition.id().equals(WeaponSkinDefinitions.SEASONS_BEATINGS)
                    && context.target() != null && context.target().hasEffect(MobEffects.SLOWNESS))
                return List.of(new OutgoingDamageContribution(definition.id(), SEASONS_OUTGOING));
            return List.<OutgoingDamageContribution>of();
        }).orElse(List.of());
    }

    @Override
    public List<IncomingDamageContribution> resolveIncoming(com.cosmicpve.combat.api.CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null) return List.of();
        return skins.resolve(context.target().getMainHandItem()).map(definition -> {
            if (definition.id().equals(WeaponSkinDefinitions.STORMBRINGER))
                return List.of(new IncomingDamageContribution(definition.id(), STORM_INCOMING_MULTIPLIER));
            if (definition.id().equals(WeaponSkinDefinitions.SEASONS_BEATINGS))
                return List.of(new IncomingDamageContribution(definition.id(), SEASONS_INCOMING_MULTIPLIER));
            return List.<IncomingDamageContribution>of();
        }).orElse(List.of());
    }

    @Override
    public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_VALID_HIT || event.attacker() == null || event.target() == null
                || event.combatResult().map(result -> result.context().category() != AttackCategory.MELEE).orElse(true)) {
            return List.of();
        }
        var definition = event.combatResult()
                .flatMap(result -> skins.resolve(result.context().weaponSnapshot().stack())).orElse(null);
        if (definition == null) return List.of();
        if (definition.id().equals(WeaponSkinDefinitions.MAUIS_HOOK)) {
            if (!stacks.canStealOne(event.target(), event.attacker(), event.serverTick())) return List.of();
            return List.of(candidate(definition.id(), MAUI_STEAL_CHANCE, activation -> stacks.stealOne(
                    activation.event().target(), activation.event().attacker(),
                    Optional.of(activation.event().attacker().getUUID()), activation.event().serverTick())));
        }
        if (definition.id().equals(WeaponSkinDefinitions.STORMBRINGER)) {
            return List.of(candidate(definition.id(), STORM_CHANCE, activation -> activateStorm(activation.event())));
        }
        return List.of();
    }

    private ProcCandidate candidate(
            net.minecraft.resources.Identifier id, double chance, com.cosmicpve.combat.proc.ProcAction action) {
        return new ProcCandidate(id, ProcHook.ON_VALID_HIT, chance, Optional.empty(), 0L,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.empty(),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL, id, action,
                new ProcProvenance(ProcSourceKind.WEAPON_SKIN, id));
    }

    private void activateStorm(ProcEvent event) {
        var target = event.target();
        var parent = event.combatResult().orElse(null);
        if (target == null || parent == null || target.isDeadOrDying()
                || !(target.level() instanceof ServerLevel level)) return;
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.setPos(target.getX(), target.getY(), target.getZ());
            level.addFreshEntity(bolt);
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,
                STORM_SLOWNESS_TICKS, STORM_SLOWNESS_AMPLIFIER));
        childActions.deliverTrue(parent.context(), target,
                stormPacket(),
                RecursionPolicy.NO_PROCS);
    }

    public static TrueDamagePacket stormPacket() {
        return TrueDamagePacket.standard(WeaponSkinDefinitions.STORMBRINGER, STORM_TRUE_DAMAGE);
    }
}
