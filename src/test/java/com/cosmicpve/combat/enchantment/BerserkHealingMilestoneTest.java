package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import java.util.List;
import org.junit.jupiter.api.Test;

class BerserkHealingMilestoneTest {
    @Test void berserkUsesCanonicalLevelChanceDurationAndOrdinaryUniqueSpec() {
        assertEquals(List.of(.01, .02, .03, .04, .05),
                java.util.stream.IntStream.rangeClosed(1, 5).mapToObj(BerserkBehavior::chance).toList());
        assertEquals(List.of(20, 40, 60, 80, 100),
                java.util.stream.IntStream.rangeClosed(1, 5).mapToObj(BerserkBehavior::durationTicks).toList());
        var spec = CosmicEnchantmentSpecs.find(ModEnchantments.BERSERK.identifier()).orElseThrow();
        assertEquals(5, spec.maxLevel());
        assertEquals(CosmicEnchantmentTier.UNIQUE, spec.tier());
        assertEquals("axe", spec.equipmentApplicability());
        assertTrue(spec.randomPoolEligible());
        assertFalse(CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.BERSERK));
    }

    @Test void healingUsesFixedChanceVanillaAbsorptionLevelsAndOrdinaryUniqueSpec() {
        assertEquals(.10, HealingBehavior.chance(1));
        assertEquals(.10, HealingBehavior.chance(2));
        assertEquals(4, HealingBehavior.absorptionHp(1));
        assertEquals(8, HealingBehavior.absorptionHp(2));
        assertEquals(0, HealingBehavior.amplifier(1));
        assertEquals(1, HealingBehavior.amplifier(2));
        assertEquals(80, HealingBehavior.DURATION_TICKS);
        var spec = CosmicEnchantmentSpecs.find(ModEnchantments.HEALING.identifier()).orElseThrow();
        assertEquals(2, spec.maxLevel());
        assertEquals(CosmicEnchantmentTier.UNIQUE, spec.tier());
        assertEquals("crossbow", spec.equipmentApplicability());
        assertTrue(spec.randomPoolEligible());
        assertFalse(CosmicEnchantmentTableRewards.POOL.contains(ModEnchantments.HEALING));
    }
}
