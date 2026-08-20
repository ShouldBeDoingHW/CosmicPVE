package com.cosmicpve.combat.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.pipeline.AttackSequenceService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CombatContextTest {
    @Test
    void childCarriesParentSequenceAndLimitedRecursionPolicy() {
        var sequences = new AttackSequenceService();
        var rootSequence = sequences.nextRoot();
        var root = new CombatContext(
                null, null, null, null, Optional.empty(), null,
                AttackCategory.MELEE, DamageChannel.ORDINARY, Set.of(CombatFlag.MELEE),
                WeaponSnapshot.empty(), EffectiveEnchantments.EMPTY,
                rootSequence.id(), OptionalLong.empty(), RecursionPolicy.NORMAL);

        var childSequence = sequences.nextChild(root.attackSequenceId());
        var child = root.child(null, DamageChannel.ORDINARY, RecursionPolicy.LIMITED_OFFENSIVE_REROLL, childSequence);

        assertEquals(root.attackSequenceId(), child.parentSequenceId().orElseThrow());
        assertTrue(child.flags().contains(CombatFlag.CHILD_ATTACK));
        assertTrue(child.recursionPolicy().permitsProcs());
        assertFalse(child.recursionPolicy().permitsParentProc());
        assertFalse(RecursionPolicy.NO_PROCS.permitsProcs());
    }

    @Test
    void trueChildRetainsPlayerAttributionAndNoProcsPolicy() {
        var sequences = new AttackSequenceService();
        var playerId = java.util.UUID.randomUUID();
        var rootSequence = sequences.nextRoot();
        var root = new CombatContext(
                null, null, null, null, Optional.of(playerId), null,
                AttackCategory.PROJECTILE, DamageChannel.ORDINARY, Set.of(CombatFlag.PROJECTILE),
                WeaponSnapshot.empty(), EffectiveEnchantments.EMPTY,
                rootSequence.id(), OptionalLong.empty(), RecursionPolicy.NORMAL);

        var child = root.child(
                null, DamageChannel.TRUE, RecursionPolicy.NO_PROCS,
                sequences.nextChild(root.attackSequenceId()));

        assertEquals(playerId, child.attributedPlayerId().orElseThrow());
        assertEquals(root.attackSequenceId(), child.parentSequenceId().orElseThrow());
        assertEquals(DamageChannel.TRUE, child.channel());
        assertFalse(child.recursionPolicy().permitsProcs());
    }
}
