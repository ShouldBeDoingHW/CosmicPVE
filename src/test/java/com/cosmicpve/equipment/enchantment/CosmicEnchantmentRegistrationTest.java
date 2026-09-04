package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                Map.entry("venom", 3),
                Map.entry("aegis", 6),
                Map.entry("eagle_eye", 6),
                Map.entry("rage", 6),
                Map.entry("molten", 4),
                Map.entry("nutrition", 3),
                Map.entry("glowing", 1),
                Map.entry("obsidianshield", 2),
                Map.entry("oxygenate", 2),
                Map.entry("armored", 4),
                Map.entry("death_pact", 5),
                Map.entry("auto_smelt", 1),
                Map.entry("experience", 3),
                Map.entry("telekinesis", 1),
                Map.entry("blessed", 4),
                Map.entry("implants", 3),
                Map.entry("trap", 3),
                Map.entry("cactus", 2),
                Map.entry("gears", 3),
                Map.entry("permafrost", 6),
                Map.entry("mortal_coil", 2),
                Map.entry("self_destruct", 3),
                Map.entry("phoenix", 3),
                Map.entry("divine_immolation", 4),
                Map.entry("virus", 3),
                Map.entry("devour", 4),
                Map.entry("undead_ruse", 10),
                Map.entry("obliterate", 3),
                Map.entry("soul_tether", 3),
                Map.entry("dodge", 5),
                Map.entry("leadership", 10),
                Map.entry("hero_killer", 3),
                Map.entry("soul_siphon", 4),
                Map.entry("blackout", 4),
                Map.entry("ender_walker", 5),
                Map.entry("voodoo", 6),
                Map.entry("sniper", 5),
                Map.entry("snare", 4),
                Map.entry("plague_carrier", 7),
                Map.entry("hex", 5),
                Map.entry("inversion", 4),
                Map.entry("spirit_link", 7));
        for (var entry : expected.entrySet()) {
            var resource = getClass().getClassLoader()
                    .getResourceAsStream("data/cosmicpve/enchantment/" + entry.getKey() + ".json");
            assertNotNull(resource, entry.getKey());
            try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals(entry.getValue().intValue(), json.get("max_level").getAsInt());
                if (!entry.getKey().equals("armored")) assertEquals(0, json.getAsJsonObject("effects").size());
                if (entry.getKey().equals("aegis")) {
                    assertEquals("#cosmicpve:enchantable/chestplate", json.get("supported_items").getAsString());
                    assertEquals("chest", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("eagle_eye")) {
                    assertEquals("#cosmicpve:enchantable/bow_or_crossbow", json.get("supported_items").getAsString());
                } else if (entry.getKey().equals("rage")) {
                    assertEquals("#cosmicpve:enchantable/sword_or_axe", json.get("supported_items").getAsString());
                } else if (entry.getKey().equals("molten")) {
                    assertEquals("#minecraft:enchantable/armor", json.get("supported_items").getAsString());
                    assertEquals("armor", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("nutrition")) {
                    assertEquals("#cosmicpve:enchantable/leggings", json.get("supported_items").getAsString());
                    assertEquals("legs", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("oxygenate")) {
                    assertEquals("#cosmicpve:enchantable/pickaxe", json.get("supported_items").getAsString());
                    assertEquals("mainhand", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("armored")) {
                    var effect = json.getAsJsonObject("effects").getAsJsonArray("minecraft:damage_protection")
                            .get(0).getAsJsonObject().getAsJsonObject("effect").getAsJsonObject("value");
                    assertEquals(0.5, effect.get("base").getAsDouble());
                    assertEquals(0.5, effect.get("per_level_above_first").getAsDouble());
                } else if (entry.getKey().equals("death_pact")) {
                    assertEquals("#cosmicpve:enchantable/chestplate", json.get("supported_items").getAsString());
                } else if (entry.getKey().equals("phoenix")) {
                    assertEquals("#minecraft:enchantable/foot_armor", json.get("supported_items").getAsString());
                    assertEquals("feet", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("sniper")) {
                    assertEquals("#cosmicpve:enchantable/bow_or_crossbow", json.get("supported_items").getAsString());
                } else if (entry.getKey().equals("snare")) {
                    assertEquals("#cosmicpve:enchantable/crossbow", json.get("supported_items").getAsString());
                } else if (entry.getKey().equals("plague_carrier")) {
                    assertEquals("#cosmicpve:enchantable/leggings", json.get("supported_items").getAsString());
                    assertEquals("legs", json.getAsJsonArray("slots").get(0).getAsString());
                } else if (entry.getKey().equals("obsidian_destroyer")) {
                    assertEquals("#cosmicpve:enchantable/pickaxe", json.get("supported_items").getAsString());
                    assertEquals(4, json.get("max_level").getAsInt());
                } else if (entry.getKey().equals("dominate")) {
                    assertEquals("#cosmicpve:enchantable/bow_or_crossbow", json.get("supported_items").getAsString());
                    assertEquals(4, json.get("max_level").getAsInt());
                } else if (entry.getKey().equals("spirit_link")) {
                    assertEquals("#cosmicpve:enchantable/helmet_or_chestplate", json.get("supported_items").getAsString());
                    assertEquals("head", json.getAsJsonArray("slots").get(0).getAsString());
                    assertEquals("chest", json.getAsJsonArray("slots").get(1).getAsString());
                }
            }
        }
        assertEquals(64, CosmicEnchantmentSpecs.ALL.size());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.EXECUTE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.ANGELIC.tier());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.LIGHTNING.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.ENDER_SHIFT.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DOUBLESTRIKE.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.BLEED.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.LUCK.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.POISON.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.PUMMEL.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.GREATSWORD.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.INSANITY.tier());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.VENOM.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.AEGIS.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.EAGLE_EYE.tier());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.RAGE.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.MOLTEN.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.NUTRITION.tier());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.GLOWING.tier());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.OBSIDIANSHIELD.tier());
        assertEquals(CosmicEnchantmentTier.SIMPLE, CosmicEnchantmentSpecs.OXYGENATE.tier());
        assertEquals("any_armor", CosmicEnchantmentSpecs.MOLTEN.equipmentApplicability());
        assertEquals("leggings", CosmicEnchantmentSpecs.NUTRITION.equipmentApplicability());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.OXYGENATE.equipmentApplicability());
        assertTrue(CosmicEnchantmentSpecs.OXYGENATE.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.ARMORED.tier());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.DEATH_PACT.tier());
        assertFalse(CosmicEnchantmentSpecs.DEATH_PACT.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.AUTO_SMELT.tier());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.EXPERIENCE.tier());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.AUTO_SMELT.equipmentApplicability());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.EXPERIENCE.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.TELEKINESIS.tier());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.TELEKINESIS.equipmentApplicability());
        assertEquals(1, CosmicEnchantmentSpecs.TELEKINESIS.maxLevel());
        assertTrue(CosmicEnchantmentSpecs.TELEKINESIS.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.BLESSED.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.BLESSED.equipmentApplicability());
        assertEquals(4, CosmicEnchantmentSpecs.BLESSED.maxLevel());
        assertEquals(CosmicEnchantmentTier.ULTIMATE, CosmicEnchantmentSpecs.IMPLANTS.tier());
        assertEquals("helmet", CosmicEnchantmentSpecs.IMPLANTS.equipmentApplicability());
        assertEquals(3, CosmicEnchantmentSpecs.IMPLANTS.maxLevel());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.TRAP.tier());
        assertEquals("sword", CosmicEnchantmentSpecs.TRAP.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.CACTUS.tier());
        assertEquals("leggings", CosmicEnchantmentSpecs.CACTUS.equipmentApplicability());
        assertEquals(2, CosmicEnchantmentSpecs.CACTUS.maxLevel());
        assertTrue(CosmicEnchantmentSpecs.CACTUS.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.GEARS.tier());
        assertEquals("boots", CosmicEnchantmentSpecs.GEARS.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.PERMAFROST.tier());
        assertEquals("chestplate", CosmicEnchantmentSpecs.PERMAFROST.equipmentApplicability());
        assertFalse(CosmicEnchantmentSpecs.PERMAFROST.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.MORTAL_COIL.tier());
        assertEquals("helmet", CosmicEnchantmentSpecs.MORTAL_COIL.equipmentApplicability());
        assertFalse(CosmicEnchantmentSpecs.MORTAL_COIL.tier().extractableByBlackScroll());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.SELF_DESTRUCT.tier());
        assertEquals("leggings", CosmicEnchantmentSpecs.SELF_DESTRUCT.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.PHOENIX.tier());
        assertEquals("boots", CosmicEnchantmentSpecs.PHOENIX.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.DIVINE_IMMOLATION.tier());
        assertEquals("sword", CosmicEnchantmentSpecs.DIVINE_IMMOLATION.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentSpecs.VIRUS.tier());
        assertEquals("bow_or_crossbow", CosmicEnchantmentSpecs.VIRUS.equipmentApplicability());
        assertEquals(CosmicEnchantmentTier.LEGENDARY, CosmicEnchantmentSpecs.DEVOUR.tier());
        assertEquals("axe", CosmicEnchantmentSpecs.DEVOUR.equipmentApplicability());
        assertFalse(CosmicEnchantmentSpecs.PHOENIX.tier().extractableByBlackScroll());
        assertFalse(CosmicEnchantmentSpecs.DIVINE_IMMOLATION.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.SELF_DESTRUCT.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.VIRUS.tier().extractableByBlackScroll());
        assertTrue(CosmicEnchantmentSpecs.DEVOUR.tier().extractableByBlackScroll());
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
