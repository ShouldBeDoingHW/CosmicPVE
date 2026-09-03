package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import org.junit.jupiter.api.Test;

class InversionFoundationTest {
    @Test void metadataAndChanceAreCanonical() {
        assertEquals(ModEnchantments.INVERSION.identifier(), CosmicEnchantmentSpecs.INVERSION.id());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.INVERSION.tier());
        assertEquals("sword", CosmicEnchantmentSpecs.INVERSION.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.INVERSION.maxLevel());
        assertEquals(.01, InversionEventBridge.chance(1), 1e-12);
        assertEquals(.02, InversionEventBridge.chance(2), 1e-12);
        assertEquals(.03, InversionEventBridge.chance(3), 1e-12);
        assertEquals(.04, InversionEventBridge.chance(4), 1e-12);
        assertTrue(CosmicEnchantmentSpecs.INVERSION.tier().extractableByBlackScroll());
    }
}
