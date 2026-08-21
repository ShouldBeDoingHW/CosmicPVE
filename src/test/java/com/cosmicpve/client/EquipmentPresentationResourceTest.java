package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class EquipmentPresentationResourceTest {
    @Test void representativeVanillaArmorInventoryModelsUseArmorSetTint() throws Exception {
        for (String material : List.of("leather", "chainmail", "iron", "golden", "diamond", "netherite"))
            for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
                String path = "/assets/minecraft/items/" + material + "_" + piece + ".json";
                try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(path)))) {
                    assertTrue(JsonParser.parseReader(reader).toString().contains("cosmicpve:armor_set"), path);
                }
            }
    }

    @Test void heroicAndMauiPresentationResourcesAreExplicit() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/heroic_crystal.json")))) {
            assertEquals("minecraft:item/amethyst_shard", JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("textures").get("layer0").getAsString());
        }
        for (String piece : List.of("helmet", "chestplate", "leggings", "boots"))
            assertNotNull(getClass().getResource("/assets/cosmicpve/items/heroic_leather_" + piece + ".json"));
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/mauis_hook.json")))) {
            var display = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("display");
            assertTrue(display.getAsJsonObject("firstperson_righthand").getAsJsonArray("scale").get(0).getAsFloat() < 0);
            assertTrue(display.getAsJsonObject("thirdperson_lefthand").getAsJsonArray("scale").get(0).getAsFloat() < 0);
        }
    }
}
