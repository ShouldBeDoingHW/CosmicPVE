package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.registry.ModEnchantments;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class Step8GHeroicEnchantmentsTest {
    @Test void canonicalHeroicTierAndReplacementPairsAreExact() {
        assertEquals(0xFF00A2, CosmicEnchantmentTier.HEROIC.tooltipColor());
        assertTrue(CosmicEnchantmentTier.HEROIC.allowsRates(1, 1));
        assertTrue(CosmicEnchantmentTier.HEROIC.allowsRates(100, 100));
        assertFalse(CosmicEnchantmentTier.HEROIC.extractableByBlackScroll());
        assertEquals(11, HeroicEnchantments.PAIRS.size());
        assertEquals(Set.of(ModEnchantments.DEEP_BLEED.identifier(), ModEnchantments.MIGHTY_CACTUS.identifier(),
                ModEnchantments.PALADIN_ARMORED.identifier(), ModEnchantments.BLIGHTED_VIRUS.identifier(),
                ModEnchantments.ALIEN_IMPLANTS.identifier(), ModEnchantments.LETHAL_SNIPER.identifier(),
                ModEnchantments.ETERNAL_SNARE.identifier(), ModEnchantments.PERMANENT_EXECUTE.identifier(),
                ModEnchantments.MIGHTY_CLEAVE.identifier(), ModEnchantments.FORBIDDEN_CURSE.identifier(),
                ModEnchantments.GODLY_OVERLOAD.identifier()),
                HeroicEnchantments.PAIRS.stream().map(HeroicEnchantments.Pair::heroic).collect(Collectors.toSet()));
        for (var pair : HeroicEnchantments.PAIRS) {
            assertEquals(pair.heroic(), HeroicEnchantments.heroicFor(pair.ordinary()).orElseThrow());
            assertEquals(pair.ordinary(), HeroicEnchantments.ordinaryFor(pair.heroic()).orElseThrow());
        }
    }

    @Test void heroicMetadataAndActivatedStormcallerAreCanonical() {
        assertEquals(76, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(11, CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier() == CosmicEnchantmentTier.HEROIC).count());
        assertEquals(6, CosmicEnchantmentSpecs.DEEP_BLEED.maxLevel());
        assertEquals("axe", CosmicEnchantmentSpecs.DEEP_BLEED.equipmentApplicability());
        assertEquals(2, CosmicEnchantmentSpecs.MIGHTY_CACTUS.maxLevel());
        assertEquals("leggings", CosmicEnchantmentSpecs.MIGHTY_CACTUS.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.PALADIN_ARMORED.maxLevel());
        assertEquals("any_armor", CosmicEnchantmentSpecs.PALADIN_ARMORED.equipmentApplicability());
        assertEquals(3, CosmicEnchantmentSpecs.BLIGHTED_VIRUS.maxLevel());
        assertEquals(3, CosmicEnchantmentSpecs.ALIEN_IMPLANTS.maxLevel());
        assertEquals(5, CosmicEnchantmentSpecs.LETHAL_SNIPER.maxLevel());
        assertEquals(4, CosmicEnchantmentSpecs.ETERNAL_SNARE.maxLevel());
        assertEquals(5, CosmicEnchantmentSpecs.PERMANENT_EXECUTE.maxLevel());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.STORMCALLER.tier());
        assertTrue(CosmicEnchantmentSpecs.STORMCALLER.randomPoolEligible());
    }

    @Test void deepBleedAndMightyCactusUseExactProcValues() {
        assertArrayEquals(new double[] {.07, .08, .09, .10, .11, .12},
                java.util.stream.IntStream.rangeClosed(1, 6).mapToDouble(DeepBleedBehavior::chance).toArray(), 1e-12);
        assertEquals(140, DeepBleedBehavior.STACK_DURATION_TICKS);
        assertEquals(.04, MightyCactusBehavior.chance(1), 1e-12);
        assertEquals(.08, MightyCactusBehavior.chance(2), 1e-12);
        assertEquals(3.0, MightyCactusBehavior.TRUE_DAMAGE, 1e-12);
    }

    @Test void paladinBlightedAndAlienUseExactLevelScaling() {
        assertEquals(.01, PaladinArmoredBehavior.chance(1), 1e-12);
        assertEquals(.16, PaladinArmoredBehavior.chance(16), 1e-12);
        assertEquals(50, PaladinArmoredBehavior.WEAKNESS_TICKS);
        assertEquals(1.25, BlightedVirusBehavior.trueDamage(1), 1e-12);
        assertEquals(1.50, BlightedVirusBehavior.trueDamage(2), 1e-12);
        assertEquals(1.75, BlightedVirusBehavior.trueDamage(3), 1e-12);
        assertEquals(100, BlightedVirusBehavior.REGENERATION_TICKS);
        assertEquals(82, AlienImplantsBehavior.intervalTicks(1));
        assertEquals(64, AlienImplantsBehavior.intervalTicks(2));
        assertEquals(46, AlienImplantsBehavior.intervalTicks(3));
        assertEquals(1.0F, AlienImplantsBehavior.HEAL_AMOUNT);
    }

    @Test void lethalEternalAndPermanentUseExactCombatValues() {
        assertEquals(.75, LethalSniperBehavior.HEADSHOT_FRACTION, 1e-12);
        assertEquals(.08, LethalSniperBehavior.projectileBonus(1), 1e-12);
        assertEquals(.40, LethalSniperBehavior.projectileBonus(5), 1e-12);
        assertEquals(.10, LethalSniperBehavior.MELEE_BONUS, 1e-12);
        assertEquals(200, LethalSniperBehavior.MELEE_BUFF_TICKS);
        assertEquals(.04, EternalSnareBehavior.chance(1), 1e-12);
        assertEquals(.16, EternalSnareBehavior.chance(4), 1e-12);
        assertEquals(35, EternalSnareBehavior.DURATION_TICKS);
        assertEquals(.15, EternalSnareBehavior.MELEE_VULNERABILITY, 1e-12);
        assertEquals(.12, PermanentExecuteBehavior.DAMAGE_BONUS, 1e-12);
        assertEquals(.15, PermanentExecuteBehavior.blessThreshold(5), 1e-12);
        assertEquals(.10, PermanentExecuteBehavior.blessChance(5), 1e-12);
        assertEquals(.12, PermanentExecuteBehavior.bonus(5, 11, 20), 1e-12);
        assertEquals(0.0, PermanentExecuteBehavior.bonus(5, 12, 20), 1e-12);
    }
}
