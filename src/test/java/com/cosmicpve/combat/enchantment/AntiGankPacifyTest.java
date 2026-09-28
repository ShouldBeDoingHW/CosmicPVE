package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import org.junit.jupiter.api.Test;

class AntiGankPacifyTest {
    @Test void canonicalMetadataAndCaps() {
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.ANTI_GANK.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.ANTI_GANK.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.ANTI_GANK.maxLevel());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.PACIFY.tier());
        assertEquals("bow", CosmicEnchantmentSpecs.PACIFY.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.PACIFY.maxLevel());
        for (int level = 1; level <= 4; level++) {
            assertEquals(level * .03, AntiGankBehavior.bonus(level, 100), 1e-12);
            assertEquals(level * .02, PacifyBehavior.chance(level), 1e-12);
            assertEquals(-level * .01, PacifyBehavior.penalty(level), 1e-12);
        }
        assertEquals(0.0, AntiGankBehavior.bonus(4, 0));
        assertEquals(.01, AntiGankBehavior.bonus(4, 1));
        assertEquals(.10, AntiGankBehavior.bonus(4, 10));
        assertEquals(.12, AntiGankBehavior.bonus(4, 12));
        assertEquals(60, PacifyBehavior.DURATION_TICKS);
    }
}
