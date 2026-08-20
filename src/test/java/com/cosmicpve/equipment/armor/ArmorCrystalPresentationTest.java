package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

class ArmorCrystalPresentationTest {
    @Test void bundledModelUsesNetherStarTexture() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/models/item/armor_set_crystal.json")))) {
            var texture = JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("textures").get("layer0").getAsString();
            assertEquals("minecraft:item/nether_star", texture);
        }
    }
    @Test void cosmicBookModelUsesBookTextureAndRarityTintSource() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/items/cosmic_enchantment_book.json")))) {
            var model = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("model");
            assertEquals("cosmicpve:cosmic_book", model.getAsJsonArray("tints").get(0)
                    .getAsJsonObject().get("type").getAsString());
        }
    }
}
