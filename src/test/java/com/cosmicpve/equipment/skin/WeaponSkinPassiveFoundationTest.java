package com.cosmicpve.equipment.skin;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.content.definition.stack.StackDefinitionData;
import com.cosmicpve.content.definition.stack.StackPolarity;
import com.cosmicpve.content.definition.stack.StackRefreshPolicy;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.EnchantmentSuppressionService;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WeaponSkinPassiveFoundationTest {
    @Test void feedingFrenzyDefinitionIsPositiveIndependentAndThreeSeconds() throws Exception {
        var definition = stack("feeding_frenzy");
        assertEquals(StackPolarity.POSITIVE, definition.polarity());
        assertEquals(10, definition.maximumStacks());
        assertEquals(60, definition.durationTicks());
        assertEquals(StackRefreshPolicy.INDEPENDENT, definition.refreshPolicy());
        assertTrue(definition.transferable());
        assertTrue(definition.cleansable());
        assertFalse(definition.persistent());
    }

    @Test void hysteriaDefinitionAndExactRedirectContractArePinned() throws Exception {
        var definition = stack("hysteria");
        assertEquals(StackPolarity.NEGATIVE, definition.polarity());
        assertEquals(8, definition.maximumStacks());
        assertEquals(80, definition.durationTicks());
        assertEquals(StackRefreshPolicy.INDEPENDENT, definition.refreshPolicy());
        assertEquals(.005, HysteriaRedirectService.chance(1), 1e-12);
        assertEquals(.04, HysteriaRedirectService.chance(8), 1e-12);
        assertEquals(.04, HysteriaRedirectService.chance(20), 1e-12);
        assertEquals(7.25, HysteriaRedirectService.redirectSnapshot(7.25));
        assertTrue(HysteriaRedirectService.CHILD_EXCLUSIONS.contains(ModEnchantments.CLEAVE.identifier()));
        assertTrue(HysteriaRedirectService.CHILD_EXCLUSIONS.contains(ModEnchantments.MIGHTY_CLEAVE.identifier()));
        assertTrue(HysteriaRedirectService.CHILD_EXCLUSIONS.contains(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertTrue(HysteriaRedirectService.CHILD_EXCLUSIONS.contains(ModEnchantments.INVERSION.identifier()));
        assertTrue(HysteriaRedirectService.CHILD_EXCLUSIONS.contains(WeaponSkinCombatResolver.HYSTERIA));
    }

    @Test void grimSuppressionTierSelectionExcludesHigherAndHeroicTiers() {
        var suppressed = Set.of(CosmicEnchantmentTier.ELITE, CosmicEnchantmentTier.UNIQUE);
        assertTrue(EnchantmentSuppressionService.suppressesTier(suppressed, CosmicEnchantmentTier.ELITE));
        assertTrue(EnchantmentSuppressionService.suppressesTier(suppressed, CosmicEnchantmentTier.UNIQUE));
        assertFalse(EnchantmentSuppressionService.suppressesTier(suppressed, CosmicEnchantmentTier.LEGENDARY));
        assertFalse(EnchantmentSuppressionService.suppressesTier(suppressed, CosmicEnchantmentTier.MASTERY));
        assertFalse(EnchantmentSuppressionService.suppressesTier(suppressed, CosmicEnchantmentTier.HEROIC));
        assertEquals(140, EnchantmentSuppressionService.refreshedExpiry(Long.MIN_VALUE, 100, 40));
        assertEquals(150, EnchantmentSuppressionService.refreshedExpiry(130, 110, 40));
        assertTrue(EnchantmentSuppressionService.activeAt(150, 149));
        assertFalse(EnchantmentSuppressionService.activeAt(150, 150));
    }

    private StackDefinitionData stack(String name) throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/data/cosmicpve/cosmicpve/stack_definitions/" + name + ".json")))) {
            return StackDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
        }
    }
}
