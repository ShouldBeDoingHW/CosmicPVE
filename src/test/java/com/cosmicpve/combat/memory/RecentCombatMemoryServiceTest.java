package com.cosmicpve.combat.memory;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.enchantment.RageBehavior;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecentCombatMemoryServiceTest {
    @Test
    void committedPositiveHealthLossIsTheOnlyRecordedBoundary() {
        var service = new RecentCombatMemoryService(200);
        var source = UUID.randomUUID();
        var target = UUID.randomUUID();
        assertFalse(service.recordCommitted(source, target, 0.0, 10));
        assertFalse(service.recordCommitted(source, target, -1.0, 11));
        assertFalse(service.recordCommitted(source, target, Double.NaN, 12));
        assertEquals(0, service.count(source, target, 12, 200));
        assertTrue(service.recordCommitted(source, target, 0.5, 13));
        assertEquals(1, service.count(source, target, 13, 200));
    }

    @Test
    void rageTracksRelationshipsIndependentlyAndDoesNotConsumeHistory() {
        var service = new RecentCombatMemoryService(200);
        var zombieA = UUID.randomUUID();
        var zombieB = UUID.randomUUID();
        var player = UUID.randomUUID();
        service.record(zombieA, player, 100);
        service.record(zombieA, player, 110);
        service.record(zombieB, player, 115);
        assertFalse(RageBehavior.active(6, service.count(zombieA, player, 120, RageBehavior.windowTicks(6))));
        assertEquals(2, service.count(zombieA, player, 120, 200));
        assertEquals(1, service.count(zombieB, player, 120, 200));
        service.record(zombieA, player, 120);
        assertTrue(RageBehavior.active(6, service.count(zombieA, player, 120, RageBehavior.windowTicks(6))));
        assertEquals(3, service.count(zombieA, player, 120, RageBehavior.windowTicks(6)));
        assertEquals(3, service.count(zombieA, player, 120, RageBehavior.windowTicks(6)));
        service.record(zombieA, player, 121);
        assertEquals(RageBehavior.bonus(6), RageBehavior.active(6, service.count(zombieA, player, 121, 200))
                ? RageBehavior.bonus(6) : 0.0);
    }

    @Test
    void levelWindowsUseInclusiveRollingServerTicksAndCleanupDropsStaleRelationships() {
        assertEquals(100, RageBehavior.windowTicks(1));
        assertEquals(200, RageBehavior.windowTicks(6));
        assertEquals(0.06, RageBehavior.bonus(1), 1e-12);
        assertEquals(0.11, RageBehavior.bonus(6), 1e-12);
        var service = new RecentCombatMemoryService(200);
        var source = UUID.randomUUID();
        var target = UUID.randomUUID();
        service.record(source, target, 100);
        service.record(source, target, 101);
        service.record(source, target, 102);
        assertEquals(3, service.count(source, target, 200, RageBehavior.windowTicks(1)));
        assertEquals(2, service.count(source, target, 201, RageBehavior.windowTicks(1)));
        assertTrue(service.mostRecent(source, target, 201, 100).isPresent());
        service.prune(303);
        assertEquals(0, service.relationshipCount());
        service.record(source, target, 500);
        service.prune(1); // A new server tick timeline cannot inherit the previous server's memory.
        assertEquals(0, service.relationshipCount());
    }
}
