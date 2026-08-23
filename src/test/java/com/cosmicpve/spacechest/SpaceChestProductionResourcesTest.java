package com.cosmicpve.spacechest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpaceChestProductionResourcesTest {
    @Test void productionTablesHaveCanonicalRowsAndWeights() {
        var expected = Map.of("ultimate", new int[]{12, 94}, "legendary", new int[]{14, 112}, "mastery", new int[]{14, 109});
        expected.forEach((tier, values) -> {
            JsonObject table = resource(tier);
            assertEquals(values[0], table.getAsJsonArray("entries").size());
            int total = table.getAsJsonArray("entries").asList().stream()
                    .mapToInt(entry -> entry.getAsJsonObject().get("weight").getAsInt()).sum();
            assertEquals(values[1], total);
        });
    }

    @Test void generatedArmorUsesRandomValidLevelsAndBookRowsAreUnexamined() {
        String ultimate = resource("ultimate").toString();
        assertTrue(ultimate.contains("random_valid"));
        assertTrue(ultimate.contains("\"maximum_rarity\":\"ultimate\""));
        String mastery = resource("mastery").toString();
        assertTrue(mastery.contains("\"rarity\":\"mastery\""));
        assertTrue(mastery.contains("30000000") && mastery.contains("50000000"));
        for (String tier : List.of("ultimate", "legendary", "mastery")) {
            String json = resource(tier).toString();
            assertTrue(json.contains("unexamined_book"));
            assertTrue(!json.contains("cosmic_book"));
        }
    }

    @Test void everyRarityBookRowKeepsCanonicalRarityWeightAndQuantity() {
        assertBookRows("ultimate", List.of(
                new BookRow("ultimate", 10, 1), new BookRow("legendary", 5, 1), new BookRow("elite", 10, 2)));
        assertBookRows("legendary", List.of(
                new BookRow("ultimate", 10, 2), new BookRow("legendary", 10, 1)));
        assertBookRows("mastery", List.of(
                new BookRow("ultimate", 10, 2), new BookRow("legendary", 10, 2), new BookRow("mastery", 10, 1)));
    }

    private static void assertBookRows(String tableTier, List<BookRow> expected) {
        var actual = resource(tableTier).getAsJsonArray("entries").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(entry -> entry.getAsJsonObject("reward").get("type").getAsString().equals("unexamined_book"))
                .map(entry -> new BookRow(entry.getAsJsonObject("reward").get("rarity").getAsString(),
                        entry.get("weight").getAsInt(),
                        entry.has("minimum_quantity") ? entry.get("minimum_quantity").getAsInt() : 1))
                .toList();
        assertEquals(expected, actual);
    }

    private record BookRow(String rarity, int weight, int quantity) {}

    private static JsonObject resource(String tier) {
        String path = "/data/cosmicpve/cosmicpve/reward_tables/space_chest/" + tier + ".json";
        try (var reader = new InputStreamReader(SpaceChestProductionResourcesTest.class.getResourceAsStream(path),
                StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) { throw new AssertionError(exception); }
    }
}
