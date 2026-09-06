package com.cosmicpve.adventure;

import com.google.gson.JsonParser;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WoodlandPolishResourceTest {
    @Test void plainsTintAndExactGroundFeaturesAreWiredWithoutVanillaTrees() throws Exception {
        try (var in = getClass().getResourceAsStream("/data/cosmicpve/worldgen/biome/dense_woodlands.json")) {
            var biome = JsonParser.parseString(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals(.8, biome.get("temperature").getAsDouble());
            assertEquals(.4, biome.get("downfall").getAsDouble());
            var effects = biome.getAsJsonObject("effects");
            assertFalse(effects.has("grass_color"));
            assertFalse(effects.has("foliage_color"));
            assertFalse(effects.has("grass_color_modifier"));
            var stage = biome.getAsJsonArray("features").get(9).getAsJsonArray();
            var vanilla = stage.asList().stream().map(e -> e.getAsString()).filter(s -> s.startsWith("minecraft:")).collect(Collectors.toSet());
            assertEquals(Set.of("minecraft:patch_grass_plain", "minecraft:patch_tall_grass_2", "minecraft:flower_plains",
                    "minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal", "minecraft:patch_pumpkin",
                    "minecraft:patch_sugar_cane"), vanilla);
            assertEquals(stage.size(), stage.asList().stream().distinct().count());
        }
    }
}
