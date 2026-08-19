package com.cosmicpve.combat.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Structurally immutable facts shared by every stage of one damage sequence. */
public record CombatContext(
        @Nullable Entity directSource,
        @Nullable Entity creditedSource,
        @Nullable LivingEntity attacker,
        @Nullable LivingEntity target,
        Optional<UUID> attributedPlayerId,
        @Nullable DamageSource damageSource,
        AttackCategory category,
        DamageChannel channel,
        Set<CombatFlag> flags,
        WeaponSnapshot weaponSnapshot,
        long attackSequenceId,
        OptionalLong parentSequenceId,
        RecursionPolicy recursionPolicy) {

    public CombatContext {
        attributedPlayerId = attributedPlayerId == null ? Optional.empty() : attributedPlayerId;
        category = Objects.requireNonNull(category);
        channel = Objects.requireNonNull(channel);
        flags = Set.copyOf(flags);
        weaponSnapshot = Objects.requireNonNull(weaponSnapshot);
        if (attackSequenceId <= 0) {
            throw new IllegalArgumentException("Attack sequence IDs must be positive");
        }
        parentSequenceId = parentSequenceId == null ? OptionalLong.empty() : parentSequenceId;
        recursionPolicy = Objects.requireNonNull(recursionPolicy);
    }

    public CombatContext child(
            @Nullable LivingEntity childTarget,
            DamageChannel childChannel,
            RecursionPolicy childPolicy,
            AttackSequence childSequence) {
        var childFlags = new HashSet<>(flags);
        childFlags.add(CombatFlag.CHILD_ATTACK);
        return new CombatContext(
                directSource,
                creditedSource,
                attacker,
                childTarget,
                attributedPlayerId,
                damageSource,
                category,
                childChannel,
                childFlags,
                weaponSnapshot,
                childSequence.id(),
                childSequence.parentId(),
                childPolicy);
    }
}
