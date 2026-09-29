package com.cosmicpve.conquest;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConquestResourceTest {
    @Test void productionTableContainsOnlySupportedCanonicalRowsAtOriginalWeights() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/conquest.json")) {
            assertNotNull(stream);
            var entries = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("entries");
            assertEquals(21, entries.size());
            int totalWeight = 0;
            int unexaminedRows = 0;
            var normalized = new java.util.ArrayList<String>();
            for (var value : entries) {
                var entry = value.getAsJsonObject();
                totalWeight += entry.get("weight").getAsInt();
                var reward = entry.getAsJsonObject("reward");
                String type = reward.get("type").getAsString();
                if (type.equals("unexamined_book")) unexaminedRows++;
                assertFalse(reward.toString().contains("boss"));
                assertFalse(reward.toString().contains("gkit"));
                int minimum = entry.has("minimum_quantity") ? entry.get("minimum_quantity").getAsInt() : 1;
                int maximum = entry.has("maximum_quantity") ? entry.get("maximum_quantity").getAsInt() : minimum;
                normalized.add(entry.get("weight").getAsInt() + "|" + minimum + "|" + maximum + "|" + reward);
            }
            assertEquals(223, totalWeight, "production includes the Swag Bag and excludes the deferred weight-3 Abandoned Spaceship row");
            assertEquals(5, unexaminedRows);
            assertEquals(List.of(
                    "15|2|2|{\"type\":\"unexamined_book\",\"rarity\":\"simple\"}",
                    "15|2|2|{\"type\":\"unexamined_book\",\"rarity\":\"unique\"}",
                    "12|2|2|{\"type\":\"unexamined_book\",\"rarity\":\"elite\"}",
                    "12|1|1|{\"type\":\"unexamined_book\",\"rarity\":\"ultimate\"}",
                    "8|1|1|{\"type\":\"unexamined_book\",\"rarity\":\"legendary\"}",
                    "15|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:mystery_simple_spawner\"}",
                    "15|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:trial_portal\"}",
                    "15|2|2|{\"type\":\"static_item\",\"item\":\"cosmicpve:trial_portal\"}",
                    "10|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:transmog_scroll\"}",
                    "18|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:white_scroll\"}",
                    "10|1|1|{\"type\":\"black_scroll\",\"success_rate\":50}",
                    "6|1|1|{\"type\":\"black_scroll\",\"success_rate\":75}",
                    "10|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:repair_scroll\"}",
                    "10|1|1|{\"type\":\"space_chest\",\"rarity\":\"legendary\"}",
                    "12|1|1|{\"type\":\"space_chest\",\"rarity\":\"ultimate\"}",
                    "5|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:heroic_crystal\"}",
                    "9|1|1|{\"type\":\"random_trial_trinket\",\"trinket_tier\":1}",
                    "6|1|1|{\"type\":\"enchanted_black_scroll\",\"success_rate\":50}",
                    "10|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:personal_vault_unlock\"}",
                    "6|2|2|{\"type\":\"static_item\",\"item\":\"cosmicpve:personal_vault_unlock\"}",
                    "4|1|1|{\"type\":\"static_item\",\"item\":\"cosmicpve:cosmic_swag_bag\"}"), normalized);
        }
    }

    @Test void flareAndChestPresentationResourcesExist() {
        assertNotNull(getClass().getClassLoader().getResource("assets/cosmicpve/items/conquest_chest_flare.json"));
        assertNotNull(getClass().getClassLoader().getResource("assets/cosmicpve/models/item/conquest_chest_flare.json"));
        assertNotNull(getClass().getClassLoader().getResource("assets/cosmicpve/blockstates/conquest_chest.json"));
        assertEquals(80.0F, ConquestChestBlock.DESTROY_TIME);
        assertEquals(1_200.0F, ConquestChestBlock.EXPLOSION_RESISTANCE);
        assertEquals(0xBF0000, ConquestFlareItem.COLOR);
    }

    @Test void conquestChestRetainsItsPickaxeMiningTag() throws Exception {
        var resources = getClass().getClassLoader().getResources(
                "data/minecraft/tags/block/mineable/pickaxe.json");
        boolean found = false;
        while (resources.hasMoreElements()) {
            try (var stream = resources.nextElement().openStream()) {
                var tag = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                found |= tag.getAsJsonArray("values").asList().stream()
                        .anyMatch(value -> value.getAsString().equals("cosmicpve:conquest_chest"));
            }
        }
        assertTrue(found, "the additive pickaxe tag must include cosmicpve:conquest_chest");
    }

    @Test void calibratedProgressPreservesSlowToolsAndCompressesOnlyEndgameAcceleration() {
        float iron = 6.0F / ConquestChestBlock.DESTROY_TIME / 30.0F;
        float diamond = 8.0F / ConquestChestBlock.DESTROY_TIME / 30.0F;
        float netheriteEfficiencyFive = 35.0F / ConquestChestBlock.DESTROY_TIME / 30.0F;
        assertEquals(iron, ConquestChestBlock.calibrateDestroyProgress(iron));
        assertTrue(ConquestChestBlock.calibrateDestroyProgress(diamond) > iron);
        assertEquals(1.0F / 200.0F,
                ConquestChestBlock.calibrateDestroyProgress(netheriteEfficiencyFive), 0.000001F);
    }
}
