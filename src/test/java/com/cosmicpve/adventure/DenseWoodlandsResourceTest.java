package com.cosmicpve.adventure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/** Guards the 1.21.11 dynamic-registry shape that previously prevented every world from loading. */
class DenseWoodlandsResourceTest {
    @Test void biomeUsesCurrentEmptyGenerationCollections() throws Exception {
        JsonObject biome = resource("data/cosmicpve/worldgen/biome/dense_woodlands.json");
        assertTrue(biome.get("has_precipitation").isJsonPrimitive());
        assertTrue(biome.get("temperature").isJsonPrimitive());
        assertTrue(biome.get("downfall").isJsonPrimitive());
        assertTrue(biome.get("effects").isJsonObject());
        assertTrue(biome.get("spawners").isJsonObject());
        assertTrue(biome.get("spawn_costs").isJsonObject());
        assertTrue(biome.get("carvers").isJsonArray(), "1.21.11 BiomeGenerationSettings requires a carver list");
        assertEquals(0, biome.getAsJsonArray("carvers").size());
        assertTrue(biome.get("features").isJsonArray());
    }

    @Test void dimensionReferencesTheBundledBiomeAndCustomNoiseSettings() throws Exception {
        JsonObject dimension = resource("data/cosmicpve/dimension/dense_woodlands.json");
        assertEquals("minecraft:overworld", dimension.get("type").getAsString());
        JsonObject generator = dimension.getAsJsonObject("generator");
        assertEquals("minecraft:noise", generator.get("type").getAsString());
        assertEquals("cosmicpve:dense_woodlands", generator.get("settings").getAsString());
        assertEquals("cosmicpve:dense_woodlands",
                generator.getAsJsonObject("biome_source").get("biome").getAsString());
    }

    @Test void adventureItemsHaveCurrentItemDefinitions() throws Exception {
        assertItemModel("call_of_forest_10", "minecraft:item/goat_horn");
        assertItemModel("call_of_forest_20", "minecraft:item/goat_horn");
        assertItemModel("call_of_forest_30", "minecraft:item/goat_horn");
        assertItemModel("dense_woodlands_scrap", "cosmicpve:item/dense_woodlands_scrap");
        var compass = resource("assets/cosmicpve/items/adventure_compass.json").getAsJsonObject("model");
        assertEquals("minecraft:range_dispatch", compass.get("type").getAsString());
        assertEquals("minecraft:compass", compass.get("property").getAsString());
        assertEquals("lodestone", compass.get("target").getAsString());
        assertEquals(33, compass.getAsJsonArray("entries").size());
    }

    private static void assertItemModel(String id, String expectedModel) throws Exception {
        JsonObject model = resource("assets/cosmicpve/items/" + id + ".json")
                .getAsJsonObject("model");
        assertEquals("minecraft:model", model.get("type").getAsString(), id);
        assertEquals(expectedModel, model.get("model").getAsString(), id);
    }

    private static JsonObject resource(String path) throws Exception {
        var stream = DenseWoodlandsResourceTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
