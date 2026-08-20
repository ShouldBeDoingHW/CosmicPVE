package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CosmicEnchantmentRegistrationTest {
    @Test
    void realDefinitionsHaveExpectedMaxLevelsAndEmptyVanillaEffects() throws Exception {
        var expected = Map.of(
                "execute", 5,
                "angelic", 5,
                "lightning", 4,
                "ender_shift", 3,
                "doublestrike", 3,
                "bleed", 6,
                "luck", 10,
                "poison", 3,
                "pummel", 3);
        for (var entry : expected.entrySet()) {
            var resource = getClass().getClassLoader()
                    .getResourceAsStream("data/cosmicpve/enchantment/" + entry.getKey() + ".json");
            assertNotNull(resource, entry.getKey());
            try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals(entry.getValue().intValue(), json.get("max_level").getAsInt());
                assertEquals(0, json.getAsJsonObject("effects").size());
            }
        }
        assertEquals(9, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.EXECUTE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.ANGELIC.tier());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.LIGHTNING.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.ENDER_SHIFT.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DOUBLESTRIKE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.BLEED.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.LUCK.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.POISON.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.PUMMEL.tier());
    }

    @Test
    void doublestrikeDamageTypeOnlyJoinsHurtCooldownBypass() throws Exception {
        assertTagContains("bypasses_cooldown", ModEnchantments.DOUBLESTRIKE.identifier().toString(), true);
        assertTagContains("bypasses_armor", ModEnchantments.DOUBLESTRIKE.identifier().toString(), false);
        assertTagContains("bypasses_shield", ModEnchantments.DOUBLESTRIKE.identifier().toString(), false);
    }

    private void assertTagContains(String tag, String id, boolean expected) throws Exception {
        var resource = getClass().getClassLoader()
                .getResourceAsStream("data/minecraft/tags/damage_type/" + tag + ".json");
        assertNotNull(resource, tag);
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            var values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("values");
            boolean found = values.asList().stream().anyMatch(value -> value.getAsString().equals(id));
            assertEquals(expected, found, tag);
        }
    }
}
