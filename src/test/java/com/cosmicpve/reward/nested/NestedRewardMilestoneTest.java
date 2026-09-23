package com.cosmicpve.reward.nested;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.registry.ModDataComponents;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class NestedRewardMilestoneTest {
    @Test void creationKitCatalogIsExactAndDrawsFiveDistinctRows() {
        assertEquals(List.of(
                "trial_portal_1|10|1", "trial_portal_2|10|2", "trial_portal_3|10|3",
                "skip_1|6|1", "skip_2|4|1", "skip_3|2|1",
                "time_1|6|1", "time_3|4|1", "time_5|2|1",
                "fame_33|6|1", "fame_66|4|1", "fame_100|2|1",
                "madness_1|6|1", "madness_2|4|1", "madness_3|2|1"),
                describe(TrialsCreationKitRewards.ROWS));
        Set<String> ids = TrialsCreationKitRewards.ROWS.stream().map(WeightedNestedRewards.Row::id).collect(Collectors.toSet());
        assertEquals(15, ids.size());
        assertTrue(ids.stream().noneMatch(id -> id.contains("insurance")));
        boolean distinctPortalRowsCoexist = false;
        for (int seed = 0; seed < 1_000; seed++) {
            var drawn = WeightedNestedRewards.selectRows(TrialsCreationKitRewards.ROWS, 5, true, RandomSource.create(seed));
            assertEquals(5, drawn.size());
            assertEquals(5, drawn.stream().map(WeightedNestedRewards.Row::id).distinct().count());
            if (drawn.stream().anyMatch(row -> row.id().equals("trial_portal_1"))
                    && drawn.stream().anyMatch(row -> row.id().equals("trial_portal_2"))) distinctPortalRowsCoexist = true;
        }
        assertTrue(distinctPortalRowsCoexist);
        assertEquals(3, TrialsCreationKitRewards.preview().stream()
                .filter(stack -> stack.is(com.cosmicpve.registry.ModItems.TRIAL_PORTAL.get()))
                .findFirst().orElseThrow().getCount());
    }

    @Test void swagBagCatalogIsExactAndIndependentDrawsMayRepeat() {
        assertEquals(List.of(
                "unexamined_simple_1|5|1", "unexamined_simple_2|5|2", "unexamined_simple_3|5|3",
                "unexamined_unique_1|5|1", "unexamined_unique_2|5|2", "unexamined_unique_3|5|3",
                "unexamined_elite_1|5|1", "unexamined_elite_2|5|2", "unexamined_elite_3|5|3",
                "unexamined_ultimate_1|5|1", "unexamined_ultimate_2|5|2", "unexamined_ultimate_3|5|3",
                "unexamined_legendary_1|5|1", "unexamined_legendary_2|5|2", "unexamined_legendary_3|5|3",
                "unexamined_heroic_1|5|1", "unexamined_mastery_1|5|1",
                "money_note_1_to_1000000|25|1", "personal_vault_unlock|30|1",
                "xp_bottle_3000|15|1", "xp_bottle_5000|15|1", "xp_bottle_7500|15|1", "xp_bottle_10000|15|1",
                "transmog_scroll|25|1", "white_scroll|50|1", "repair_scroll_1|25|1", "repair_scroll_2|25|2",
                "armor_orb_50|10|1", "weapon_orb_50|10|1"), describe(CosmicSwagBagRewards.ROWS));
        boolean repeated = false;
        for (int seed = 0; seed < 500; seed++) {
            var drawn = WeightedNestedRewards.selectRows(CosmicSwagBagRewards.ROWS, 3, false, RandomSource.create(seed));
            assertEquals(3, drawn.size());
            if (drawn.stream().map(WeightedNestedRewards.Row::id).distinct().count() < 3) repeated = true;
        }
        assertTrue(repeated);
        assertEquals(3, CosmicSwagBagRewards.preview().stream()
                .filter(stack -> ItemStack.isSameItemSameComponents(stack,
                        CosmicSwagBagRewards.ROWS.getFirst().preview()))
                .findFirst().orElseThrow().getCount());
        assertEquals(2, CosmicSwagBagRewards.preview().stream()
                .filter(stack -> stack.is(com.cosmicpve.registry.ModItems.REPAIR_SCROLL.get()))
                .findFirst().orElseThrow().getCount());
        assertTrue(CosmicSwagBagRewards.preview().stream().anyMatch(stack -> stack.has(ModDataComponents.BANKNOTE.get())
                && stack.get(ModDataComponents.BANKNOTE.get()).valueCents() == 100_000_000L));
    }

    @Test void allFourCanonicalProductionSourcesHaveOneRealSwagBagRow() throws Exception {
        assertSource("adventure/advanced_dense_woodlands", 10);
        assertSource("conquest", 4);
        assertSource("trial/impossible", 4);
        assertSource("trial/demonic_development", 7);
    }

    @Test void presentationAndBanknotePayloadFollowCanonicalContracts() throws Exception {
        var kit = new ItemStack(com.cosmicpve.registry.ModItems.TRIALS_CREATION_KIT.get());
        var bag = new ItemStack(com.cosmicpve.registry.ModItems.COSMIC_SWAG_BAG.get());
        assertTrue(kit.hasFoil() && bag.hasFoil());
        assertEquals("Trials Creation Kit", kit.getHoverName().getString());
        assertEquals(0xFA9E05, kit.getHoverName().getStyle().getColor().getValue());
        assertTrue(kit.getHoverName().getStyle().isBold());
        assertEquals("Cosmic Swag Bag", bag.getHoverName().getString());
        int[] colors = {0x20F5EF, 0xFFFFFF, 0xE994F2};
        var glyphs = bag.getHoverName().getSiblings();
        assertEquals("Cosmic Swag Bag".length(), glyphs.size());
        for (int index = 0; index < glyphs.size(); index++) {
            assertEquals(colors[index % 3], glyphs.get(index).getStyle().getColor().getValue());
            assertTrue(glyphs.get(index).getStyle().isBold() && glyphs.get(index).getStyle().isItalic());
        }
        assertEquals("The ultimate stash of goodies needed to create the most powerful trial portal in existence. Right click to open!",
                NestedRewardContainerItem.lore(NestedRewardContainerItem.Kind.TRIALS_CREATION_KIT).getFirst().getString());
        assertEquals("Contains 3 spicy items to give you a little more swagger in your life. Click to open!",
                NestedRewardContainerItem.lore(NestedRewardContainerItem.Kind.COSMIC_SWAG_BAG).getFirst().getString());
        assertEquals("minecraft:block/end_portal_frame", model("trials_creation_kit"));
        assertEquals("minecraft:block/pumpkin", model("cosmic_swag_bag"));

        var money = CosmicSwagBagRewards.ROWS.stream().filter(row -> row.id().equals("money_note_1_to_1000000"))
                .findFirst().orElseThrow();
        assertEquals(25, money.weight());
        boolean arbitraryValue = false;
        for (int seed = 0; seed < 100; seed++) {
            var note = money.create(RandomSource.create(seed));
            long cents = note.get(ModDataComponents.BANKNOTE.get()).valueCents();
            assertTrue(cents >= 100 && cents <= 100_000_000 && cents % 100 == 0);
            if (cents % 1_000_000 != 0) arbitraryValue = true;
        }
        assertTrue(arbitraryValue, "Money row is not rounded to a fixed $10,000 increment");
    }

    private static String model(String id) throws Exception {
        try (var stream = NestedRewardMilestoneTest.class.getResourceAsStream("/assets/cosmicpve/items/" + id + ".json")) {
            assertNotNull(stream);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("model").get("model").getAsString();
        }
    }

    private static void assertSource(String path, int weight) throws Exception {
        String resource = "/data/cosmicpve/cosmicpve/reward_tables/" + path + ".json";
        try (var stream = NestedRewardMilestoneTest.class.getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            var entries = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("entries");
            var matches = new java.util.ArrayList<com.google.gson.JsonObject>();
            for (var element : entries) {
                var row = element.getAsJsonObject();
                var reward = row.getAsJsonObject("reward");
                if (reward.has("item") && reward.get("item").getAsString().equals("cosmicpve:cosmic_swag_bag"))
                    matches.add(row);
            }
            assertEquals(1, matches.size(), path);
            var row = matches.getFirst();
            assertEquals(weight, row.get("weight").getAsInt(), path);
            assertEquals(1, row.has("minimum_quantity") ? row.get("minimum_quantity").getAsInt() : 1, path);
            assertEquals(1, row.has("maximum_quantity") ? row.get("maximum_quantity").getAsInt() : 1, path);
        }
    }

    private static List<String> describe(List<WeightedNestedRewards.Row> rows) {
        return rows.stream().map(row -> row.id() + "|" + row.weight() + "|" + row.preview().getCount()).toList();
    }
}
