package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class UnexaminedBookOpeningServiceTest {
    private final UnexaminedBookOpeningService service =
            new UnexaminedBookOpeningService(new BookOpeningRateService());

    @Test void rollSelectsOnlySameTierAndAlwaysUsesAValidLevel() {
        var available = List.of(CosmicEnchantmentSpecs.LIGHTNING, CosmicEnchantmentSpecs.GLOWING,
                CosmicEnchantmentSpecs.OXYGENATE, CosmicEnchantmentSpecs.MOLTEN);
        var random = RandomSource.create(8128L);
        for (int i = 0; i < 300; i++) {
            var result = service.roll(CosmicEnchantmentTier.SIMPLE, available, random, null).orElseThrow();
            assertEquals(CosmicEnchantmentTier.SIMPLE, result.enchantment().tier());
            assertTrue(result.level() >= 1 && result.level() <= result.enchantment().maxLevel());
            assertTrue(result.rates().successRate() >= 1 && result.rates().successRate() <= 100);
            assertTrue(result.rates().destroyRate() >= 1 && result.rates().destroyRate() <= 100);
        }
    }

    @Test void masteryRevealsOnlyCurrentMasterySpecsAtValidLevelsAndRestrictedRates() {
        var mastery = List.of(CosmicEnchantmentSpecs.DEATH_PACT, CosmicEnchantmentSpecs.PERMAFROST,
                CosmicEnchantmentSpecs.MORTAL_COIL, CosmicEnchantmentSpecs.PHOENIX,
                CosmicEnchantmentSpecs.DIVINE_IMMOLATION, CosmicEnchantmentSpecs.SOUL_TETHER,
                CosmicEnchantmentSpecs.HERO_KILLER, CosmicEnchantmentSpecs.SOUL_SIPHON,
                CosmicEnchantmentSpecs.BLACKOUT, CosmicEnchantmentSpecs.NEUTRALIZE);
        var seen = new java.util.HashSet<CosmicEnchantmentSpec>();
        var random = RandomSource.create(1L);
        for (int i = 0; i < 200; i++) {
            var result = service.roll(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.ALL,
                    random, null).orElseThrow();
            assertTrue(mastery.contains(result.enchantment()));
            seen.add(result.enchantment());
            assertTrue(result.level() >= 1 && result.level() <= result.enchantment().maxLevel());
            assertTrue(result.rates().successRate() <= 49);
            assertTrue(result.rates().destroyRate() >= 51);
        }
        assertEquals(new java.util.HashSet<>(mastery), seen);
    }

    @Test void changedRaritiesUseOnlyTheirCanonicalBookPools() {
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.AUTO_SMELT.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.EXPERIENCE.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.POISON.tier());
        assertTrue(CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier() == CosmicEnchantmentTier.SIMPLE)
                .noneMatch(spec -> spec == CosmicEnchantmentSpecs.AUTO_SMELT
                        || spec == CosmicEnchantmentSpecs.EXPERIENCE));
        assertTrue(CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier() == CosmicEnchantmentTier.ELITE)
                .noneMatch(spec -> spec == CosmicEnchantmentSpecs.POISON));
    }

    @Test void heroicPoolContainsAllReplacementsWithOrdinaryRateRanges() {
        var seen = new java.util.HashSet<CosmicEnchantmentSpec>();
        var random = RandomSource.create(8808L);
        for (int i = 0; i < 400; i++) {
            var result = service.roll(CosmicEnchantmentTier.HEROIC, CosmicEnchantmentSpecs.ALL,
                    random, null).orElseThrow();
            seen.add(result.enchantment());
            assertTrue(result.level() >= 1 && result.level() <= result.enchantment().maxLevel());
            assertTrue(result.rates().successRate() >= 1 && result.rates().successRate() <= 100);
            assertTrue(result.rates().destroyRate() >= 1 && result.rates().destroyRate() <= 100);
        }
        assertEquals(new java.util.HashSet<>(java.util.List.of(CosmicEnchantmentSpecs.DEEP_BLEED,
                CosmicEnchantmentSpecs.MIGHTY_CACTUS, CosmicEnchantmentSpecs.PALADIN_ARMORED,
                CosmicEnchantmentSpecs.BLIGHTED_VIRUS, CosmicEnchantmentSpecs.ALIEN_IMPLANTS,
                CosmicEnchantmentSpecs.LETHAL_SNIPER, CosmicEnchantmentSpecs.ETERNAL_SNARE,
                CosmicEnchantmentSpecs.PERMANENT_EXECUTE, CosmicEnchantmentSpecs.MIGHTY_CLEAVE,
                CosmicEnchantmentSpecs.FORBIDDEN_CURSE, CosmicEnchantmentSpecs.GODLY_OVERLOAD,
                CosmicEnchantmentSpecs.PLANETARY_DEATHBRINGER, CosmicEnchantmentSpecs.TITAN_TRAP)), seen);
        assertTrue(service.roll(CosmicEnchantmentTier.ELITE, java.util.List.of(CosmicEnchantmentSpecs.STORMCALLER),
                random, null).isPresent());
    }

    @Test void revealedBookUsesTheExistingActualCosmicBookComponentPath() {
        var result = service.roll(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.ALL,
                RandomSource.create(7L), null).orElseThrow();
        ItemStack revealed = UnexaminedBooks.revealed(result);
        assertTrue(revealed.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get()));
        var data = revealed.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        assertEquals(result.enchantment().id(), data.enchantmentId());
        assertEquals(result.level(), data.level());
        assertEquals(result.rates().successRate(), data.successRate());
        assertEquals(result.rates().destroyRate(), data.destroyRate());
        assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(revealed).isEmpty());
    }

    @Test void feedbackFireworkLaunchesWithoutADamagingExplosionPayload() {
        var fireworks = UnexaminedEnchantmentBookItem.fireworkStack().get(DataComponents.FIREWORKS);
        assertEquals(1, fireworks.flightDuration());
        assertTrue(fireworks.explosions().isEmpty());
        assertTrue(UnexaminedEnchantmentBookItem.FORCE_GLINT);
        assertEquals(64, new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get()).getMaxStackSize());
    }
}
