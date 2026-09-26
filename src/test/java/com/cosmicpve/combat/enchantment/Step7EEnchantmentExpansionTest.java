package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.combat.proc.ProcChance;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import org.junit.jupiter.api.Test;

class Step7EEnchantmentExpansionTest {
    @Test void undeadRuseUsesHighestLevelChanceTieredCapAndFortyFiveSecondLifetime() {
        assertEquals(.005, UndeadRuseBehavior.chance(1), 1e-12);
        assertEquals(.05, UndeadRuseBehavior.chance(10), 1e-12);
        for (int level = 1; level <= 4; level++) assertEquals(1, UndeadRuseBehavior.cap(level));
        for (int level = 5; level <= 9; level++) assertEquals(2, UndeadRuseBehavior.cap(level));
        assertEquals(3, UndeadRuseBehavior.cap(10));
        assertEquals(900, UndeadRuseBehavior.LIFETIME_TICKS);
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.UNDEAD_RUSE.tier());
    }

    @Test void obliterateUsesStrictThresholdFixedChanceAndLevelScaledKnockback() {
        assertTrue(ObliterateBehavior.belowThreshold(3.99F, 20));
        assertFalse(ObliterateBehavior.belowThreshold(4.0F, 20));
        assertEquals(.10, ObliterateBehavior.CHANCE);
        assertEquals(3, ObliterateBehavior.intendedBlocks(1));
        assertEquals(6, ObliterateBehavior.intendedBlocks(2));
        assertEquals(9, ObliterateBehavior.intendedBlocks(3));
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.OBLITERATE.tier());
        assertEquals("all_weapons", CosmicEnchantmentSpecs.OBLITERATE.equipmentApplicability());
    }

    @Test void soulTetherHasFixedChanceCooldownDurationsSlowAndUncappedDistanceScaling() {
        assertEquals(120, SoulTetherService.durationTicks(1));
        assertEquals(140, SoulTetherService.durationTicks(2));
        assertEquals(160, SoulTetherService.durationTicks(3));
        assertEquals(.105, SoulTetherService.damageBonus(2.1), 1e-12);
        assertEquals(5.0, SoulTetherService.damageBonus(100), 1e-12);
        assertEquals(-.20, new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                SoulTetherService.MOVEMENT_MODIFIER, -.20,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).amount());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.SOUL_TETHER.tier());
        assertFalse(CosmicEnchantmentSpecs.SOUL_TETHER.tier().extractableByBlackScroll());
    }

    @Test void dodgeAndTurkeyShareOneAdditiveBaseThenLuckAppliesRelatively() {
        assertEquals(.005, DodgeProcResolver.chance(1, false), 1e-12);
        assertEquals(.025, DodgeProcResolver.chance(5, false), 1e-12);
        assertEquals(.02, DodgeProcResolver.chance(0, true), 1e-12);
        assertEquals(.045, DodgeProcResolver.chance(5, true), 1e-12);
        assertEquals(.054, ProcChance.calculate(DodgeProcResolver.chance(5, true), List.of(1.2)), 1e-12);
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.DODGE.tier());
        assertEquals("boots", CosmicEnchantmentSpecs.DODGE.equipmentApplicability());
    }

    @Test void leadershipStacksChestAndLeggingsToTwentyAndUsesOnePercentPerLevel() {
        assertEquals(0, LeadershipBehavior.aggregateLevels(0, 0));
        assertEquals(10, LeadershipBehavior.aggregateLevels(6, 4));
        assertEquals(20, LeadershipBehavior.aggregateLevels(10, 10));
        assertEquals(.20, LeadershipBehavior.bonus(20), 1e-12);
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.LEADERSHIP.tier());
        assertEquals("chestplate_or_leggings", CosmicEnchantmentSpecs.LEADERSHIP.equipmentApplicability());
    }

    @Test void allFiveJoinGenericBookCapacityTransmogAndExtractionMetadata() {
        assertEquals(83, CosmicEnchantmentSpecs.ALL.size());
        assertTrue(CosmicEnchantmentSpecs.UNDEAD_RUSE.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.OBLITERATE.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.SOUL_TETHER.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.DODGE.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.LEADERSHIP.tier().extractableByBlackScroll());
        assertEquals(10, CosmicEnchantmentSpecs.UNDEAD_RUSE.maxLevel());
        assertEquals(3, CosmicEnchantmentSpecs.OBLITERATE.maxLevel());
        assertEquals(3, CosmicEnchantmentSpecs.SOUL_TETHER.maxLevel());
        assertEquals(5, CosmicEnchantmentSpecs.DODGE.maxLevel());
        assertEquals(10, CosmicEnchantmentSpecs.LEADERSHIP.maxLevel());
    }
}
