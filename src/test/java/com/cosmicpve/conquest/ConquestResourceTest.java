package com.cosmicpve.conquest;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConquestResourceTest {
    @Test void productionTableContainsOnlySupportedCanonicalRowsAtOriginalWeights() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/conquest.json")) {
            assertNotNull(stream);
            var entries = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("entries");
            assertEquals(14, entries.size());
            int totalWeight = 0;
            int unexaminedRows = 0;
            for (var value : entries) {
                var entry = value.getAsJsonObject();
                totalWeight += entry.get("weight").getAsInt();
                var reward = entry.getAsJsonObject("reward");
                String type = reward.get("type").getAsString();
                if (type.equals("unexamined_book")) unexaminedRows++;
                assertFalse(reward.toString().contains("boss"));
                assertFalse(reward.toString().contains("gkit"));
            }
            assertEquals(140, totalWeight);
            assertEquals(5, unexaminedRows);
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
