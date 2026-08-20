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
        var expected = Map.ofEntries(
                Map.entry("execute", 5),
                Map.entry("angelic", 5),
                Map.entry("lightning", 4),
                Map.entry("ender_shift", 3),
                Map.entry("doublestrike", 3),
                Map.entry("bleed", 6),
                Map.entry("luck", 10),
                Map.entry("poison", 3),
                Map.entry("pummel", 3),
                Map.entry("greatsword", 4),
                Map.entry("insanity", 8),
                Map.entry("venom", 3));
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
        assertEquals(12, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.EXECUTE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.ANGELIC.tier());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.LIGHTNING.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.ENDER_SHIFT.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DOUBLESTRIKE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.BLEED.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.LUCK.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.POISON.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.PUMMEL.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.GREATSWORD.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.INSANITY.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.VENOM.tier());
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
