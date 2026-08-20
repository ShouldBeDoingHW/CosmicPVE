package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import com.cosmicpve.combat.api.CombatResult;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Server-authored facts for one proc dispatch. The shared random source rolls each eligible candidate once. */
public record ProcEvent(
        ProcHook hook,
        long sequenceId,
        OptionalLong parentSequenceId,
        RecursionPolicy recursionPolicy,
        UUID ownerId,
        Optional<UUID> tracePlayerId,
        long serverTick,
        List<Double> chanceMultipliers,
        Map<Identifier, Double> namedChanceMultipliers,
        List<Double> cooldownDurationMultipliers,
        Set<Identifier> excludedEffectIds,
        EffectiveEnchantments effectiveEnchantments,
        Optional<CombatResult> combatResult,
        @Nullable LivingEntity attacker,
        @Nullable LivingEntity target,
        ProcRandomSource random) {
    public ProcEvent {
        hook = Objects.requireNonNull(hook);
        if (sequenceId <= 0 || serverTick < 0) {
            throw new IllegalArgumentException("Proc sequence must be positive and server tick non-negative");
        }
        parentSequenceId = parentSequenceId == null ? OptionalLong.empty() : parentSequenceId;
        recursionPolicy = Objects.requireNonNull(recursionPolicy);
        ownerId = Objects.requireNonNull(ownerId);
        tracePlayerId = tracePlayerId == null ? Optional.empty() : tracePlayerId;
        chanceMultipliers = List.copyOf(chanceMultipliers);
        namedChanceMultipliers = Map.copyOf(namedChanceMultipliers);
        cooldownDurationMultipliers = List.copyOf(cooldownDurationMultipliers);
        excludedEffectIds = Set.copyOf(excludedEffectIds);
        effectiveEnchantments = Objects.requireNonNull(effectiveEnchantments);
        combatResult = combatResult == null ? Optional.empty() : combatResult;
        random = Objects.requireNonNull(random);
    }

    public ProcEvent(
            ProcHook hook,
            long sequenceId,
            OptionalLong parentSequenceId,
            RecursionPolicy recursionPolicy,
            UUID ownerId,
            Optional<UUID> tracePlayerId,
            long serverTick,
            List<Double> chanceMultipliers,
            List<Double> cooldownDurationMultipliers,
            Set<Identifier> excludedEffectIds,
            EffectiveEnchantments effectiveEnchantments,
            Optional<CombatResult> combatResult,
            @Nullable LivingEntity attacker,
            @Nullable LivingEntity target,
            ProcRandomSource random) {
        this(hook, sequenceId, parentSequenceId, recursionPolicy, ownerId, tracePlayerId, serverTick,
                chanceMultipliers, Map.of(), cooldownDurationMultipliers, excludedEffectIds,
                effectiveEnchantments, combatResult, attacker, target, random);
    }

    public ProcEvent(
            ProcHook hook,
            long sequenceId,
            OptionalLong parentSequenceId,
            RecursionPolicy recursionPolicy,
            UUID ownerId,
            Optional<UUID> tracePlayerId,
            long serverTick,
            List<Double> chanceMultipliers,
            Set<Identifier> excludedEffectIds,
            EffectiveEnchantments effectiveEnchantments,
            @Nullable LivingEntity attacker,
            @Nullable LivingEntity target,
            ProcRandomSource random) {
        this(hook, sequenceId, parentSequenceId, recursionPolicy, ownerId, tracePlayerId, serverTick,
                chanceMultipliers, Map.of(), List.of(1.0), excludedEffectIds, effectiveEnchantments, Optional.empty(),
                attacker, target, random);
    }
}
