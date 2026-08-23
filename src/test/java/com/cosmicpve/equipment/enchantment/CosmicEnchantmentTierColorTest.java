package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CosmicEnchantmentTierColorTest {
    @Test void tiersUseDesignColors() {
        assertEquals(0xFFFFFF, CosmicEnchantmentTier.SIMPLE.tooltipColor());
        assertEquals(0x55FF55, CosmicEnchantmentTier.UNIQUE.tooltipColor());
        assertEquals(0xA3FFF5, CosmicEnchantmentTier.ELITE.tooltipColor());
        assertEquals(0xFFFF55, CosmicEnchantmentTier.ULTIMATE.tooltipColor());
        assertEquals(0xFFAA00, CosmicEnchantmentTier.LEGENDARY.tooltipColor());
        assertEquals(0xAA0000, CosmicEnchantmentTier.MASTERY.tooltipColor());
    }
}
