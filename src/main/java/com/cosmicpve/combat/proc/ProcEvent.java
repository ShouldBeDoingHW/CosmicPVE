package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
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
        Set<Identifier> excludedEffectIds,
        EffectiveEnchantments effectiveEnchantments,
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
        excludedEffectIds = Set.copyOf(excludedEffectIds);
        effectiveEnchantments = Objects.requireNonNull(effectiveEnchantments);
        random = Objects.requireNonNull(random);
    }
}
