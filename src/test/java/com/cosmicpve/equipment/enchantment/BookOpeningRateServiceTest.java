package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class BookOpeningRateServiceTest {
    @Test void baseRollsUseTierOwnedOrdinaryAndMasteryRanges() {
        var service = new BookOpeningRateService();
        var random = RandomSource.create(4141L);
        for (int i = 0; i < 500; i++) {
            var ordinary = service.roll(new BookOpeningRateContext(CosmicEnchantmentTier.SIMPLE, null), random);
            assertTrue(ordinary.successRate() >= 1 && ordinary.successRate() <= 100);
            assertTrue(ordinary.destroyRate() >= 1 && ordinary.destroyRate() <= 100);
            var mastery = service.roll(new BookOpeningRateContext(CosmicEnchantmentTier.MASTERY, null), random);
            assertTrue(mastery.successRate() >= 1 && mastery.successRate() <= 49);
            assertTrue(mastery.destroyRate() >= 51 && mastery.destroyRate() <= 100);
        }
    }

    @Test void modifierSeamComposesInOrderAndSourceOwnsAdjustment() {
        var service = new BookOpeningRateService(List.of(
                (context, rates) -> new BookOpeningRates(rates.successRate() + 3, rates.destroyRate()),
                (context, rates) -> new BookOpeningRates(rates.successRate(), rates.destroyRate() - 2)));
        assertEquals(new BookOpeningRates(43, 73), service.applyModifiers(
                new BookOpeningRateContext(CosmicEnchantmentTier.SIMPLE, null),
                new BookOpeningRates(40, 75)));
    }

    @Test void modifiersCannotPublishRatesInvalidForTheTier() {
        var service = new BookOpeningRateService(List.of(
                (context, rates) -> new BookOpeningRates(50, rates.destroyRate())));
        assertThrows(IllegalStateException.class, () -> service.applyModifiers(
                new BookOpeningRateContext(CosmicEnchantmentTier.MASTERY, null),
                new BookOpeningRates(49, 75)));
    }
}
