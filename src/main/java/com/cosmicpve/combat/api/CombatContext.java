package com.cosmicpve.combat.api;

import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
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
        EffectiveEnchantments effectiveEnchantments,
        long attackSequenceId,
        OptionalLong parentSequenceId,
        RecursionPolicy recursionPolicy,
        Set<Identifier> excludedProcEffectIds) {

    public CombatContext {
        attributedPlayerId = attributedPlayerId == null ? Optional.empty() : attributedPlayerId;
        category = Objects.requireNonNull(category);
        channel = Objects.requireNonNull(channel);
        flags = Set.copyOf(flags);
        weaponSnapshot = Objects.requireNonNull(weaponSnapshot);
        effectiveEnchantments = Objects.requireNonNull(effectiveEnchantments);
        if (attackSequenceId <= 0) {
            throw new IllegalArgumentException("Attack sequence IDs must be positive");
        }
        parentSequenceId = parentSequenceId == null ? OptionalLong.empty() : parentSequenceId;
        recursionPolicy = Objects.requireNonNull(recursionPolicy);
        excludedProcEffectIds = Set.copyOf(excludedProcEffectIds);
    }

    public CombatContext(
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
            EffectiveEnchantments effectiveEnchantments,
            long attackSequenceId,
            OptionalLong parentSequenceId,
            RecursionPolicy recursionPolicy) {
        this(directSource, creditedSource, attacker, target, attributedPlayerId, damageSource, category, channel,
                flags, weaponSnapshot, effectiveEnchantments, attackSequenceId, parentSequenceId, recursionPolicy,
                Set.of());
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
                effectiveEnchantments,
                childSequence.id(),
                childSequence.parentId(),
                childPolicy,
                excludedProcEffectIds);
    }

    public CombatContext child(
            @Nullable LivingEntity childTarget,
            DamageChannel childChannel,
            RecursionPolicy childPolicy,
            AttackSequence childSequence,
            Set<Identifier> additionalExcludedEffects) {
        var excluded = new HashSet<>(excludedProcEffectIds);
        excluded.addAll(additionalExcludedEffects);
        var child = child(childTarget, childChannel, childPolicy, childSequence);
        return new CombatContext(
                child.directSource(), child.creditedSource(), child.attacker(), child.target(), child.attributedPlayerId(),
                child.damageSource(), child.category(), child.channel(), child.flags(), child.weaponSnapshot(),
                child.effectiveEnchantments(), child.attackSequenceId(), child.parentSequenceId(), child.recursionPolicy(),
                excluded);
    }

    public CombatContext withDamageSource(@Nullable DamageSource childDamageSource) {
        return new CombatContext(
                directSource, creditedSource, attacker, target, attributedPlayerId, childDamageSource,
                category, channel, flags, weaponSnapshot, effectiveEnchantments,
                attackSequenceId, parentSequenceId, recursionPolicy, excludedProcEffectIds);
    }

    public CombatContext withFlag(CombatFlag flag) {
        var updated = new HashSet<>(flags); updated.add(flag);
        return new CombatContext(directSource, creditedSource, attacker, target, attributedPlayerId, damageSource,
                category, channel, updated, weaponSnapshot, effectiveEnchantments, attackSequenceId,
                parentSequenceId, recursionPolicy, excludedProcEffectIds);
    }

    public boolean defensiveCosmicSuppressed() {
        return flags.contains(CombatFlag.DEFENSIVE_COSMIC_SUPPRESSED);
    }
}
