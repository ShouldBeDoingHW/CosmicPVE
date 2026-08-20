package com.cosmicpve.combat.cooldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CooldownServiceTest {
    private static final net.minecraft.resources.Identifier KEY = CosmicPVE.id("test/cooldown");

    @Test
    void startsQueriesAndExpiresOnServerTicks() {
        var service = new CooldownService();
        UUID owner = UUID.randomUUID();

        assertTrue(service.isReady(owner, KEY, 100));
        assertEquals(40, service.start(
                owner, KEY, 40, List.of(), 100, CooldownScope.EPHEMERAL_COMBAT, Optional.empty()));
        assertFalse(service.isReady(owner, KEY, 100));
        assertEquals(1, service.remainingTicks(owner, KEY, 139));
        assertTrue(service.isReady(owner, KEY, 140));
    }

    @Test
    void combinesDurationModifiersMultiplicativelyAndRoundsUp() {
        assertEquals(420, CooldownService.effectiveDuration(600, List.of(0.8, 0.875)));
        assertEquals(480, CooldownService.effectiveDuration(600, List.of(0.8)));
    }

    @Test
    void entitiesHaveIndependentCooldownState() {
        var service = new CooldownService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.set(first, KEY, 20, 10, CooldownScope.EPHEMERAL_COMBAT, Optional.empty());

        assertFalse(service.isReady(first, KEY, 10));
        assertTrue(service.isReady(second, KEY, 10));
    }

    @Test
    void clearsIndividualTemporaryAndInstanceScopes() {
        var service = new CooldownService();
        UUID owner = UUID.randomUUID();
        var persistent = CosmicPVE.id("test/persistent");
        var ephemeral = CosmicPVE.id("test/ephemeral");
        var instance = CosmicPVE.id("test/instance");
        var session = CosmicPVE.id("test/session_one");
        service.set(owner, persistent, 20, 0, CooldownScope.PERSISTENT_PLAYER, Optional.empty());
        service.set(owner, ephemeral, 20, 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty());
        service.set(owner, instance, 20, 0, CooldownScope.INSTANCE_SESSION, Optional.of(session));

        assertEquals(2, service.clearTemporary(owner));
        assertFalse(service.isReady(owner, persistent, 0));
        assertTrue(service.isReady(owner, ephemeral, 0));
        assertTrue(service.clear(owner, persistent));
        assertTrue(service.snapshot(owner, 0).isEmpty());
    }
}
