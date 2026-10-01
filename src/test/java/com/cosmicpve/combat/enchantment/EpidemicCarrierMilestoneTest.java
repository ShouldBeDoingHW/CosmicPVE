package com.cosmicpve.combat.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class EpidemicCarrierMilestoneTest {
    @Test void metadataAndReplacementUseTheCanonicalHeroicCatalog() throws Exception {
        var id = ModEnchantments.EPIDEMIC_CARRIER.identifier();
        assertEquals("cosmicpve:epidemic_carrier", id.toString());
        assertEquals(ModEnchantments.PLAGUE_CARRIER.identifier(), HeroicEnchantments.ordinaryFor(id).orElseThrow());
        assertEquals(7, CosmicEnchantmentSpecs.find(id).orElseThrow().maxLevel());
        assertEquals("leggings", CosmicEnchantmentSpecs.find(id).orElseThrow().equipmentApplicability());
        var data = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(
                getClass().getResourceAsStream("/data/cosmicpve/enchantment/epidemic_carrier.json")))).getAsJsonObject();
        assertEquals(7, data.get("max_level").getAsInt());
        assertEquals(14, HeroicEnchantments.PAIRS.size());
    }

    @Test void thresholdAndLevelMathAreExact() {
        assertFalse(EpidemicCarrierBehavior.belowThreshold(5, 20));
        assertTrue(EpidemicCarrierBehavior.belowThreshold(4.99, 20));
        assertFalse(EpidemicCarrierBehavior.belowThreshold(5.01, 20));
        assertEquals(160, EpidemicCarrierBehavior.POISON_TICKS);
        assertEquals(1, EpidemicCarrierBehavior.POISON_AMPLIFIER);
        assertEquals(600, PlagueCarrierBehavior.COOLDOWN_TICKS);
        assertEquals(.07, EpidemicCarrierBehavior.bonus(7), 1e-12);
        assertEquals(.21, EpidemicCarrierBehavior.healFraction(7), 1e-12);
        assertEquals(.01, EpidemicCarrierBehavior.bonus(1), 1e-12);
        assertEquals(.03, EpidemicCarrierBehavior.healFraction(1), 1e-12);
    }
}
