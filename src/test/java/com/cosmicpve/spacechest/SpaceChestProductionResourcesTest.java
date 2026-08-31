package com.cosmicpve.spacechest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpaceChestProductionResourcesTest {
    @Test void ultimateTableMatchesStep8FExactly() {
        assertRows("ultimate", List.of(
                r(10,"unexamined_book","ultimate"), r(10,"unexamined_book","legendary"), rq(10,2,"unexamined_book","elite"),
                r(8,"unexamined_book","unique"), r(8,"black_scroll","50"), r(4,"black_scroll","75"),
                r(4,"static_item","cosmicpve:mystery_simple_spawner"), rq(8,16,"static_item","minecraft:golden_apple"),
                r(12,"static_item","cosmicpve:repair_scroll"), r(7,"generated_equipment","1-2:ultimate:random_valid"),
                r(9,"static_item","cosmicpve:transmog_scroll"), r(10,"banknote","5000000"), r(5,"banknote","7500000"),
                r(8,"xp_bottle","2000"), r(6,"xp_bottle","4000"), r(12,"static_item","cosmicpve:trial_portal"),
                r(2,"static_item","cosmicpve:trial_trinket_time_1"), r(2,"static_item","cosmicpve:trial_trinket_insurance_1"),
                r(2,"static_item","cosmicpve:trial_trinket_skip_1"), r(3,"static_item","cosmicpve:personal_vault_unlock")));
    }

    @Test void legendaryTableMatchesStep8FExactly() {
        assertRows("legendary", List.of(
                rq(5,2,"unexamined_book","ultimate"), r(10,"unexamined_book","legendary"), rq(5,3,"unexamined_book","elite"),
                r(10,"static_item","cosmicpve:white_scroll"), r(8,"black_scroll","60"), r(4,"black_scroll","80"),
                r(5,"static_item","cosmicpve:mystery_elite_spawner"), r(3,"static_item","cosmicpve:conquest_chest_flare"),
                r(4,"static_item","cosmicpve:trial_trinket_time_1"), r(4,"static_item","cosmicpve:trial_trinket_insurance_1"),
                r(4,"static_item","cosmicpve:trial_trinket_skip_1"), r(10,"static_item","cosmicpve:repair_scroll"),
                r(8,"generated_equipment","1-3:legendary:random_valid"), r(10,"banknote","10000000"), r(5,"banknote","25000000"),
                r(8,"armor_orb","50"), r(8,"weapon_orb","50"), r(6,"xp_bottle","5000"), r(3,"xp_bottle","10000"),
                r(5,"random_vkit_crystal",""), r(15,"static_item","cosmicpve:trial_portal"),
                r(8,"static_item","cosmicpve:personal_vault_unlock")));
    }

    @Test void masteryTableMatchesEveryCurrentlyConstructibleStep8FRow() {
        assertRows("mastery", List.of(
                rq(10,2,"unexamined_book","legendary"), r(8,"unexamined_book","mastery"), r(10,"black_scroll","75"),
                r(8,"black_scroll","100"), r(6,"static_item","cosmicpve:mystery_mastery_spawner"),
                rq(10,2,"static_item","cosmicpve:repair_scroll"), r(10,"generated_equipment","2-3:legendary:random_valid"),
                r(12,"banknote","30000000"), r(6,"banknote","50000000"), r(8,"weapon_orb","75"), r(8,"armor_orb","75"),
                r(7,"xp_bottle","12000"), r(3,"xp_bottle","15000"), r(5,"static_item","cosmicpve:conquest_chest_flare"),
                r(12,"random_vkit_crystal",""), r(5,"static_item","cosmicpve:trial_portal"), rq(9,2,"static_item","cosmicpve:trial_portal"),
                r(4,"static_item","cosmicpve:trial_trinket_time_3"), r(4,"static_item","cosmicpve:trial_trinket_insurance_2"),
                r(4,"static_item","cosmicpve:trial_trinket_skip_2"), rq(10,2,"static_item","cosmicpve:white_scroll"),
                r(8,"mask","1"), r(5,"static_item","cosmicpve:cosmic_enchantment_table"),
                r(8,"enchanted_black_scroll","50"), r(5,"static_item","cosmicpve:personal_vault_unlock")));
        assertFalse(resource("mastery").toString().contains("abandoned_spaceship"));
    }

    @Test void standardTablesNeverContainMemoryChestOrSeasonalCrateHalves() {
        for (String tier : List.of("ultimate", "legendary", "mastery")) {
            String json = resource(tier).toString();
            assertFalse(json.contains("memory_chest")); assertFalse(json.contains("crate_half"));
            assertTrue(json.contains("unexamined_book")); assertFalse(json.contains("cosmic_book"));
        }
    }

    private static void assertRows(String tier, List<String> expected) {
        var actual = resource(tier).getAsJsonArray("entries").asList().stream()
                .map(element -> normalize(element.getAsJsonObject())).toList();
        assertEquals(expected, actual);
    }
    private static String normalize(JsonObject entry) {
        JsonObject reward = entry.getAsJsonObject("reward"); String type = reward.get("type").getAsString();
        String detail = switch (type) {
            case "unexamined_book", "space_chest" -> reward.get("rarity").getAsString();
            case "static_item" -> reward.get("item").getAsString();
            case "black_scroll", "armor_orb", "weapon_orb", "enchanted_black_scroll" -> reward.get("success_rate").getAsString();
            case "banknote" -> reward.get("cents").getAsString();
            case "xp_bottle" -> reward.get("experience").getAsString();
            case "mask" -> reward.get("mask_count").getAsString();
            case "generated_equipment" -> {
                JsonObject generated = reward.getAsJsonObject("generated_equipment");
                yield generated.get("minimum_enchantments").getAsString() + "-" + generated.get("maximum_enchantments").getAsString()
                        + ":" + generated.get("maximum_rarity").getAsString() + ":" + generated.get("level_mode").getAsString();
            }
            default -> "";
        };
        int quantity = entry.has("minimum_quantity") ? entry.get("minimum_quantity").getAsInt() : 1;
        return entry.get("weight").getAsInt() + "|" + quantity + "|" + type + "|" + detail;
    }
    private static String r(int weight, String type, String detail) { return weight + "|1|" + type + "|" + detail; }
    private static String rq(int weight, int quantity, String type, String detail) { return weight + "|" + quantity + "|" + type + "|" + detail; }
    private static JsonObject resource(String tier) {
        String path = "/data/cosmicpve/cosmicpve/reward_tables/space_chest/" + tier + ".json";
        try (var reader = new InputStreamReader(SpaceChestProductionResourcesTest.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) { throw new AssertionError(exception); }
    }
}
