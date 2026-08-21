package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.Test;

class EquippedPersistentEffectServiceTest {
    @Test void managedLeaseIsHiddenShortAndRecognizable() {
        var lease = EquippedPersistentEffectService.managed(MobEffects.NIGHT_VISION);
        assertEquals(60, lease.getDuration());
        assertTrue(lease.isAmbient());
        assertFalse(lease.isVisible());
        assertFalse(lease.showIcon());
        assertTrue(EquippedPersistentEffectService.isManaged(lease));
    }

    @Test void externalEffectsAreNeverMistakenForOwnedLease() {
        assertFalse(EquippedPersistentEffectService.isManaged(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, true, true)));
        assertFalse(EquippedPersistentEffectService.isManaged(
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 1, true, false, false)));
    }
}
