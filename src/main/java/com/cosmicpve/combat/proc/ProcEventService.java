package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** Creates authoritative event contexts and routes them through candidate sources and the engine. */
public final class ProcEventService {
    private final ProcEngine engine;
    private final ProcCandidateSourceRegistry candidates;
    private final AttackSequenceService sequences;
    private final EffectiveEnchantmentsResolver enchantments;
    private final ProcModifierResolver modifiers;

    public ProcEventService(
            ProcEngine engine,
            ProcCandidateSourceRegistry candidates,
            AttackSequenceService sequences,
            EffectiveEnchantmentsResolver enchantments,
            ProcModifierResolver modifiers) {
        this.engine = engine;
        this.candidates = candidates;
        this.sequences = sequences;
        this.enchantments = enchantments;
        this.modifiers = modifiers;
    }

    public void onCommittedDamage(CombatResult result) {
        if (candidates.isEmpty() || !result.isCommittedDamagingHit()
                || !(result.context().target().level() instanceof ServerLevel level)) {
            return;
        }
        var context = result.context();
        ProcRandomSource random = eventRandom(level);
        if (context.attacker() != null) {
            dispatch(create(
                    ProcHook.ON_VALID_HIT, context.attacker(), context.attacker(), context.target(),
                    context.attackSequenceId(), context.parentSequenceId(), context.recursionPolicy(),
                    context.attributedPlayerId().or(() -> tracePlayer(context.target())), context.effectiveEnchantments(), random,
                    context.excludedProcEffectIds(), Optional.of(result)));
            if (context.category() == com.cosmicpve.combat.api.AttackCategory.PROJECTILE) {
                dispatch(create(
                        ProcHook.ON_PROJECTILE_HIT, context.attacker(), context.attacker(), context.target(),
                        context.attackSequenceId(), context.parentSequenceId(), context.recursionPolicy(),
                        context.attributedPlayerId(), context.effectiveEnchantments(), random,
                        context.excludedProcEffectIds(), Optional.of(result)));
            }
            if (context.target().isDeadOrDying()) {
                dispatch(create(
                        ProcHook.ON_KILL, context.attacker(), context.attacker(), context.target(),
                        context.attackSequenceId(), context.parentSequenceId(), context.recursionPolicy(),
                        context.attributedPlayerId(), context.effectiveEnchantments(), random,
                        context.excludedProcEffectIds(), Optional.of(result)));
            }
        }
        dispatch(create(
                ProcHook.ON_DAMAGE_TAKEN, context.target(), context.attacker(), context.target(),
                context.attackSequenceId(), context.parentSequenceId(), context.recursionPolicy(),
                tracePlayer(context.target()), enchantments.resolve(context.target(), List.of()), random,
                context.excludedProcEffectIds(), Optional.of(result)));
    }

    /** Pre-calculation seam for effects, such as Devour, that alter their qualifying parent hit. */
    public ProcDispatchResult onPreDamageCalculation(CombatResult provisional) {
        var context = provisional.context();
        if (context.attacker() == null || !(context.target().level() instanceof ServerLevel level)) {
            throw new IllegalArgumentException("Pre-damage proc dispatch requires a server-side living attacker");
        }
        return dispatch(create(
                ProcHook.ON_PRE_DAMAGE_CALCULATION, context.attacker(), context.attacker(), context.target(),
                context.attackSequenceId(), context.parentSequenceId(), context.recursionPolicy(),
                context.attributedPlayerId().or(() -> tracePlayer(context.target())),
                context.effectiveEnchantments(), eventRandom(level), context.excludedProcEffectIds(),
                Optional.of(provisional)));
    }

    public ProcDispatchResult dispatchRoot(
            ProcHook hook,
            LivingEntity owner,
            @Nullable LivingEntity attacker,
            @Nullable LivingEntity target) {
        var sequence = sequences.nextRoot();
        var event = create(
                hook, owner, attacker, target, sequence.id(), sequence.parentId(), RecursionPolicy.NORMAL,
                tracePlayer(owner), enchantments.resolve(owner, List.of()), serverRandom(owner), Set.of(), Optional.empty());
        return dispatch(event);
    }

    public ProcDispatchResult dispatchDevelopment(
            LivingEntity owner,
            List<ProcCandidate> fixtures,
            RecursionPolicy policy,
            Set<Identifier> excludedEffects) {
        var sequence = sequences.nextRoot();
        var event = create(
                ProcHook.ON_VALID_HIT, owner, owner, null, sequence.id(), sequence.parentId(), policy,
                tracePlayer(owner), enchantments.resolve(owner, List.of()), serverRandom(owner), excludedEffects,
                Optional.empty());
        return engine.evaluate(event, fixtures);
    }

    public ProcDispatchResult dispatch(ProcEvent event) {
        return engine.evaluate(event, candidates.resolve(event));
    }

    public boolean hasCandidateSources() {
        return !candidates.isEmpty();
    }

    private ProcEvent create(
            ProcHook hook,
            LivingEntity owner,
            @Nullable LivingEntity attacker,
            @Nullable LivingEntity target,
            long sequenceId,
            OptionalLong parentSequenceId,
            RecursionPolicy policy,
            Optional<UUID> tracePlayerId,
            EffectiveEnchantments effectiveEnchantments,
            ProcRandomSource random,
            Set<Identifier> excludedEffects,
            Optional<CombatResult> combatResult) {
        long tick = owner.level().getServer() == null ? 0L : owner.level().getServer().getTickCount();
        ProcModifiers resolvedModifiers = modifiers.resolve(owner);
        return new ProcEvent(
                hook, sequenceId, parentSequenceId, policy, owner.getUUID(), tracePlayerId, tick,
                resolvedModifiers.chanceMultipliers(), resolvedModifiers.namedChanceMultipliers(),
                resolvedModifiers.cooldownDurationMultipliers(),
                excludedEffects, effectiveEnchantments, combatResult, attacker, target, random);
    }

    private static ProcRandomSource serverRandom(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) {
            throw new IllegalStateException("Proc dispatch is server-only");
        }
        return eventRandom(level);
    }

    private static ProcRandomSource eventRandom(ServerLevel level) {
        return ProcRandomSource.server(RandomSource.create(level.getRandom().nextLong()));
    }

    private static Optional<UUID> tracePlayer(LivingEntity entity) {
        return entity instanceof Player player ? Optional.of(player.getUUID()) : Optional.empty();
    }
}
