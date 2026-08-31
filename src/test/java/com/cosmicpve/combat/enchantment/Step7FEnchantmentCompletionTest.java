package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class Step7FEnchantmentCompletionTest {
    @Test void heroKillerUsesThreePercentPerLevelAndMasteryAxeMetadata() {
        assertEquals(.03, HeroKillerBehavior.bonus(1), 1e-12);
        assertEquals(.06, HeroKillerBehavior.bonus(2), 1e-12);
        assertEquals(.09, HeroKillerBehavior.bonus(3), 1e-12);
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.HERO_KILLER.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.HERO_KILLER.equipmentApplicability());
        assertFalse(CosmicEnchantmentSpecs.HERO_KILLER.tier().extractableByBlackScroll());
    }

    @Test void soulSiphonCooldownIsSixFiveFourThreeSeconds() {
        assertEquals(120, SoulSiphonBehavior.cooldownTicks(1));
        assertEquals(100, SoulSiphonBehavior.cooldownTicks(2));
        assertEquals(80, SoulSiphonBehavior.cooldownTicks(3));
        assertEquals(60, SoulSiphonBehavior.cooldownTicks(4));
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.SOUL_SIPHON.tier());
        assertEquals("all_weapons", CosmicEnchantmentSpecs.SOUL_SIPHON.equipmentApplicability());
        assertFalse(SoulSiphonBehavior.targetBelowHalf(10, 20));
        assertTrue(SoulSiphonBehavior.targetBelowHalf(9.99F, 20));
        assertTrue(SoulSiphonBehavior.targetBelowHalf(2, 20));
    }

    @Test void blackoutChanceAndDurationScaleAndRemainMastery() {
        for (int level = 1; level <= 4; level++) {
            assertEquals(level * .02, BlackoutBehavior.chance(level), 1e-12);
            assertEquals(level * 20, BlackoutBehavior.durationTicks(level));
        }
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.BLACKOUT.tier());
        assertEquals("sword", CosmicEnchantmentSpecs.BLACKOUT.equipmentApplicability());
    }

    @Test void enderWalkerReducesOnlyHalfAtMaximumAndUsesUltimateBootsMetadata() {
        for (int level = 1; level <= 5; level++) {
            assertEquals(1.0 - level * .10, EnderWalkerBehavior.multiplier(level), 1e-12);
        }
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.ENDER_WALKER.tier());
        assertEquals("boots", CosmicEnchantmentSpecs.ENDER_WALKER.equipmentApplicability());
    }

    @Test void voodooUsesIndependentFiveStackCapAndThreePercentPenalty() {
        for (int level = 1; level <= 6; level++) {
            assertEquals(level * .01, VoodooBehavior.chance(level), 1e-12);
        }
        for (int stacks = 1; stacks <= 5; stacks++) {
            assertEquals(stacks * -.03, VoodooBehavior.penalty(stacks), 1e-12);
        }
        assertEquals(-.15, VoodooBehavior.penalty(6), 1e-12);
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.VOODOO.tier());
        assertEquals("helmet", CosmicEnchantmentSpecs.VOODOO.equipmentApplicability());
    }

    @Test void voodooDefinitionPinsNegativeIndependentTenSecondFiveStackSemantics() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/stack_definitions/voodoo.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("negative", json.get("polarity").getAsString());
            assertEquals(5, json.get("maximum_stacks").getAsInt());
            assertEquals(200, json.get("duration_ticks").getAsInt());
            assertEquals("independent", json.get("refresh_policy").getAsString());
            assertFalse(json.get("transferable").getAsBoolean());
            assertTrue(json.get("cleansable").getAsBoolean());
            assertFalse(json.get("persistent").getAsBoolean());
        }
    }

    @Test void contentCompletePoolContainsFortySixRealEnchantments() {
        assertEquals(52, CosmicEnchantmentSpecs.ALL.size());
        assertTrue(CosmicEnchantmentSpecs.ENDER_WALKER.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.VOODOO.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.SOUL_SIPHON.tier().extractableByBlackScroll());
        assertEquals(3, CosmicEnchantmentSpecs.HERO_KILLER.maxLevel());
        assertEquals(4, CosmicEnchantmentSpecs.SOUL_SIPHON.maxLevel());
        assertEquals(4, CosmicEnchantmentSpecs.BLACKOUT.maxLevel());
        assertEquals(5, CosmicEnchantmentSpecs.ENDER_WALKER.maxLevel());
        assertEquals(6, CosmicEnchantmentSpecs.VOODOO.maxLevel());
    }
}
