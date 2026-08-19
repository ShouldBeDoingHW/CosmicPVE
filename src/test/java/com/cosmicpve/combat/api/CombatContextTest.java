package com.cosmicpve.combat.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.combat.pipeline.AttackSequenceService;
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
                WeaponSnapshot.empty(), rootSequence.id(), OptionalLong.empty(), RecursionPolicy.NORMAL);

        var childSequence = sequences.nextChild(root.attackSequenceId());
        var child = root.child(null, DamageChannel.ORDINARY, RecursionPolicy.LIMITED_OFFENSIVE_REROLL, childSequence);

        assertEquals(root.attackSequenceId(), child.parentSequenceId().orElseThrow());
        assertTrue(child.flags().contains(CombatFlag.CHILD_ATTACK));
        assertTrue(child.recursionPolicy().permitsProcs());
        assertFalse(child.recursionPolicy().permitsParentProc());
        assertFalse(RecursionPolicy.NO_PROCS.permitsProcs());
    }
}
