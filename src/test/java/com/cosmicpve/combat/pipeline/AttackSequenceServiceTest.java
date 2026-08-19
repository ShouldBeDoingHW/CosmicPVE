package com.cosmicpve.combat.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttackSequenceServiceTest {
    @Test
    void createsUniqueRootsAndLinkedChildren() {
        var sequences = new AttackSequenceService();
        var first = sequences.nextRoot();
        var second = sequences.nextRoot();
        var child = sequences.nextChild(first.id());

        assertNotEquals(first.id(), second.id());
        assertNotEquals(first.id(), child.id());
        assertNotEquals(second.id(), child.id());
        assertTrue(child.parentId().isPresent());
        assertEquals(first.id(), child.parentId().getAsLong());
    }
}
