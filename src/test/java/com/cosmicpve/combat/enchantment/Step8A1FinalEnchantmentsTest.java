package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.cooldown.CooldownService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step8A1FinalEnchantmentsTest {
    @Test
    void sniperUsesActualUpperTwentyPercentGeometryAndFivePercentPerLevel() {
        var playerSized = new AABB(2.0, 10.0, 3.0, 2.6, 12.0, 3.6);
        assertFalse(SniperBehavior.isHeadshot(playerSized, new Vec3(2.3, 11.598, 3.3)));
        assertTrue(SniperBehavior.isHeadshot(playerSized, new Vec3(2.3, 11.6, 3.3)));
        assertTrue(SniperBehavior.isHeadshot(playerSized, new Vec3(2.3, 11.9, 3.3)));

        var tallTarget = new AABB(-4.0, -2.0, 7.0, 4.0, 8.0, 15.0);
        assertFalse(SniperBehavior.isHeadshot(tallTarget, new Vec3(0.0, 5.999, 11.0)));
        assertTrue(SniperBehavior.isHeadshot(tallTarget, new Vec3(0.0, 6.0, 11.0)));
        assertFalse(SniperBehavior.isHeadshot(tallTarget, new Vec3(5.0, 9.0, 11.0)));

        assertEquals(0.05, SniperBehavior.bonus(1, true), 1.0E-12);
        assertEquals(0.25, SniperBehavior.bonus(5, true), 1.0E-12);
        assertEquals(0.0, SniperBehavior.bonus(5, false), 1.0E-12);
        assertTrue(SniperBehavior.eligibleWeapon(new ItemStack(Items.BOW)));
        assertTrue(SniperBehavior.eligibleWeapon(new ItemStack(Items.CROSSBOW)));
        assertFalse(SniperBehavior.eligibleWeapon(new ItemStack(Items.TRIDENT)));
    }

    @Test
    void snareUsesCrossbowOnlyFlatDurationAndLuckEligibleChance() {
        assertEquals(0.03, SnareBehavior.chance(1), 1.0E-12);
        assertEquals(0.06, SnareBehavior.chance(2), 1.0E-12);
        assertEquals(0.09, SnareBehavior.chance(3), 1.0E-12);
        assertEquals(0.12, SnareBehavior.chance(4), 1.0E-12);
        assertEquals(25, SnareRootService.DURATION_TICKS);
        assertEquals(0.144, com.cosmicpve.combat.proc.ProcChance.calculate(
                SnareBehavior.chance(4), List.of(LuckBehavior.chanceMultiplier(20))), 1.0E-12);
    }

    @Test
    void plagueCarrierUsesStrictThresholdPoisonScalingAndSharedCooldownMath() {
        assertTrue(PlagueCarrierBehavior.belowThreshold(4.8, 20.0));
        assertTrue(PlagueCarrierBehavior.belowThreshold(4.0, 20.0));
        assertFalse(PlagueCarrierBehavior.belowThreshold(5.0, 20.0));
        assertFalse(PlagueCarrierBehavior.belowThreshold(5.1, 20.0));
        assertFalse(PlagueCarrierBehavior.belowThreshold(0.0, 20.0));
        for (int level = 1; level <= 5; level++) assertEquals(0, PlagueCarrierBehavior.amplifier(level));
        assertEquals(1, PlagueCarrierBehavior.amplifier(6));
        assertEquals(1, PlagueCarrierBehavior.amplifier(7));
        assertEquals(List.of(60, 80, 100, 120, 140, 160, 180),
                java.util.stream.IntStream.rangeClosed(1, 7)
                        .map(PlagueCarrierBehavior::durationTicks).boxed().toList());
        assertEquals(600L, PlagueCarrierBehavior.COOLDOWN_TICKS);
        assertEquals(600L, CooldownService.effectiveDuration(PlagueCarrierBehavior.COOLDOWN_TICKS, List.of(1.0)));
        assertEquals(480L, CooldownService.effectiveDuration(PlagueCarrierBehavior.COOLDOWN_TICKS, List.of(0.8)));
    }

    @Test
    void finalOrdinaryMetadataAndExtractionRulesAreGeneric() {
        assertEquals(83, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.SNIPER.tier());
        assertEquals(5, CosmicEnchantmentSpecs.SNIPER.maxLevel());
        assertEquals("bow_or_crossbow", CosmicEnchantmentSpecs.SNIPER.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.SNARE.tier());
        assertEquals(4, CosmicEnchantmentSpecs.SNARE.maxLevel());
        assertEquals("crossbow", CosmicEnchantmentSpecs.SNARE.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.PLAGUE_CARRIER.tier());
        assertEquals(7, CosmicEnchantmentSpecs.PLAGUE_CARRIER.maxLevel());
        assertEquals("leggings", CosmicEnchantmentSpecs.PLAGUE_CARRIER.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.SNIPER.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.SNARE.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.PLAGUE_CARRIER.tier().extractableByBlackScroll());
    }
}
