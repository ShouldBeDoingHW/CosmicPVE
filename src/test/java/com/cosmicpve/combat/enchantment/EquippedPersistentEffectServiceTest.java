package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.Test;

class EquippedPersistentEffectServiceTest {
    @Test void managedLeaseIsHiddenShortAndRecognizable() {
        var lease = EquippedPersistentEffectService.managed(
                MobEffects.NIGHT_VISION, EquippedPersistentEffectService.GLOWING_LEASE_TICKS);
        assertEquals(220, lease.getDuration());
        assertTrue(lease.isAmbient());
        assertFalse(lease.isVisible());
        assertFalse(lease.showIcon());
        assertTrue(EquippedPersistentEffectService.isManaged(
                lease, EquippedPersistentEffectService.GLOWING_LEASE_TICKS));
        assertEquals(60, EquippedPersistentEffectService.OBSIDIANSHIELD_LEASE_TICKS);
    }

    @Test void externalEffectsAreNeverMistakenForOwnedLease() {
        assertFalse(EquippedPersistentEffectService.isManaged(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, true, true),
                EquippedPersistentEffectService.GLOWING_LEASE_TICKS));
        assertFalse(EquippedPersistentEffectService.isManaged(
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 1, true, false, false),
                EquippedPersistentEffectService.OBSIDIANSHIELD_LEASE_TICKS));
    }
}
