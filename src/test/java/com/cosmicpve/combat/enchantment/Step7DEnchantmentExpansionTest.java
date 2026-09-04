package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Step7DEnchantmentExpansionTest {
    @Test void selfDestructUsesStrictThresholdCardinalTntAndSixtySecondCooldown() {
        assertTrue(SelfDestructBehavior.belowThreshold(2.9F, 20.0F));
        assertFalse(SelfDestructBehavior.belowThreshold(3.0F, 20.0F));
        assertFalse(SelfDestructBehavior.belowThreshold(0.0F, 20.0F));
        assertEquals(1_200L, SelfDestructBehavior.COOLDOWN_TICKS);
        assertEquals(4, SelfDestructBehavior.OFFSETS.size());
        assertEquals(4, new HashSet<>(SelfDestructBehavior.OFFSETS).size());
        assertEquals(Set.of(
                new SelfDestructBehavior.CardinalOffset(0, -1),
                new SelfDestructBehavior.CardinalOffset(0, 1),
                new SelfDestructBehavior.CardinalOffset(1, 0),
                new SelfDestructBehavior.CardinalOffset(-1, 0)),
                new HashSet<>(SelfDestructBehavior.OFFSETS));
        assertEquals(3, CosmicEnchantmentSpecs.SELF_DESTRUCT.maxLevel());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.SELF_DESTRUCT.tier());
    }

    @Test void phoenixUsesCurrentThreeLevelSurvivalOnlyDesign() {
        assertEquals(8.0F, PhoenixBehavior.survivalHealth(20.0F), 1.0E-6F);
        assertEquals(16.0F, PhoenixBehavior.survivalHealth(40.0F), 1.0E-6F);
        assertEquals(1_800L, PhoenixBehavior.COOLDOWN_TICKS);
        assertEquals(3, CosmicEnchantmentSpecs.PHOENIX.maxLevel());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.PHOENIX.tier());
    }

    @Test void divineImmolationScalesChanceOnlyAndUsesFixedCostsAndBuff() {
        assertEquals(0.03, DivineImmolationBehavior.chance(1), 1.0E-12);
        assertEquals(0.06, DivineImmolationBehavior.chance(2), 1.0E-12);
        assertEquals(0.09, DivineImmolationBehavior.chance(3), 1.0E-12);
        assertEquals(0.12, DivineImmolationBehavior.chance(4), 1.0E-12);
        assertEquals(600L, DivineImmolationBehavior.COOLDOWN_TICKS);
        assertEquals(100L, DivineImmolationBehavior.BUFF_TICKS);
        assertEquals(5, DivineImmolationBehavior.FIRE_SECONDS);
        assertEquals(2.0, DivineImmolationBehavior.packet().amount(), 1.0E-12);
        assertTrue(DivineImmolationBehavior.packet().bypassesArmor());
        assertTrue(DivineImmolationBehavior.packet().bypassesCustomReduction());
        assertEquals(0.10, DivineImmolationBehavior.OUTGOING_BONUS, 1.0E-12);
    }

    @Test void virusUsesCanonicalTrueDamageAndExactOneHpHeal() {
        assertEquals(0.4, VirusBehavior.trueDamage(1), 1.0E-12);
        assertEquals(0.8, VirusBehavior.trueDamage(2), 1.0E-12);
        assertEquals(1.2, VirusBehavior.trueDamage(3), 1.0E-12);
        assertEquals(1.0F, VirusBehavior.HEAL_HP);
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.VIRUS.tier());
        assertEquals("bow_or_crossbow", CosmicEnchantmentSpecs.VIRUS.equipmentApplicability());
    }

    @Test void devourHasFixedChanceHungerCostHealAndLevelScaledParentBonus() {
        assertEquals(0.05, DevourBehavior.PROC_CHANCE, 1.0E-12);
        assertEquals(1, DevourBehavior.HUNGER_COST);
        assertEquals(1.0F, DevourBehavior.HEAL_HP);
        assertEquals(0.05, DevourBehavior.damageBonus(1), 1.0E-12);
        assertEquals(0.10, DevourBehavior.damageBonus(2), 1.0E-12);
        assertEquals(0.15, DevourBehavior.damageBonus(3), 1.0E-12);
        assertEquals(0.20, DevourBehavior.damageBonus(4), 1.0E-12);
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DEVOUR.tier());
    }

    @Test void allFiveUseCanonicalPoolsAndMasteryBlackScrollExclusion() {
        assertEquals(64, CosmicEnchantmentSpecs.ALL.size());
        assertTrue(CosmicEnchantmentSpecs.SELF_DESTRUCT.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.VIRUS.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.DEVOUR.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.PHOENIX.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.DIVINE_IMMOLATION.tier().extractableByBlackScroll());
    }
}
