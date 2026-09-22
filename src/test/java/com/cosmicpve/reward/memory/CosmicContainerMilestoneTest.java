package com.cosmicpve.reward.memory;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.cosmiccrate.CosmicCrateItem;
import com.cosmicpve.cosmiccrate.CosmicCrateSeason;
import com.cosmicpve.cosmiccrate.CosmicCrateSide;
import com.cosmicpve.cosmiccrate.CosmicCrateCombinationService;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.cosmicpve.equipment.enchantment.HigherLoreOrbApplicationService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.animation.LootAnimationTimeline;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class CosmicContainerMilestoneTest {
    @Test void memoryTableIsExactAndAllHalvesRemainIndependentOutcomes() {
        assertEquals(11, MemoryChestRewards.ENTRIES.size());
        assertEquals(104, MemoryChestRewards.totalWeight());
        assertEquals(List.of(36,24,12,4,4,4,4,4,4,4,4),
                MemoryChestRewards.ENTRIES.stream().map(MemoryChestRewards.Entry::weight).toList());
        assertEquals(8, MemoryChestRewards.ENTRIES.stream().filter(entry -> entry.id().endsWith("_half")).count());
        assertEquals(11, MemoryChestRewards.previewOutcomes().size());
        assertEquals(60, LootAnimationTimeline.FINAL_HOLD_TICKS);
    }

    @Test void everyCurrentlyApplicableMemoryChestSourceIsLiveAndExact() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"));
        assertMemoryRow(root.resolve("src/main/resources/data/cosmicpve/cosmicpve/reward_tables/adventure/advanced_dense_woodlands.json"),6,1);
        assertMemoryRow(root.resolve("src/main/resources/data/cosmicpve/cosmicpve/reward_tables/trial/impossible.json"),5,1);
        assertMemoryRow(root.resolve("src/main/resources/data/cosmicpve/cosmicpve/reward_tables/trial/demonic_development.json"),4,2);
        var sale = com.cosmicpve.economy.flashsale.FlashSaleCatalog.find("memory_chest").orElseThrow();
        assertTrue(sale.productionSelectable()); assertEquals(1,sale.quantity());
        assertEquals(250_000_000L,sale.lowPrice()); assertEquals(350_000_000L,sale.mediumPrice());
        assertEquals(450_000_000L,sale.highPrice());
        try (var paths = Files.walk(root.resolve("src/main"))) {
            assertFalse(paths.anyMatch(path ->
                            path.getFileName().toString().toLowerCase().contains("heroic_abandoned_spaceship_lootbag")),
                    "No existing Heroic Spaceship Lootbag catalog exists to activate; the dungeon remains deferred");
        }
    }

    @Test void memoryNameRetainsExactSixColorGradientAndWhiteSuffix() {
        var name = MemoryChestItem.displayName();
        assertEquals("Memory Chest", name.getString());
        assertEquals(List.of(0xBF0F0F,0xBF0F76,0x870FBF,0x0F26BF,0x0FB0BF,0x0FBF3E,0xFFFFFF),
                name.getSiblings().stream().map(part -> part.getStyle().getColor().getValue()).toList());
        assertTrue(name.getSiblings().stream().allMatch(part -> part.getStyle().isBold()));
    }

    @Test void eachSeasonCombinesInEitherDirectionAndInvalidPairsAreNonDestructive() {
        var service = new CosmicCrateCombinationService();
        for (var season : CosmicCrateSeason.values()) {
            for (var order : List.of(CosmicCrateSide.LEFT, CosmicCrateSide.RIGHT)) {
                var carried = half(season, order);
                var target = half(season, order.opposite());
                assertEquals(CosmicCrateCombinationService.Outcome.SUCCESS, service.combine(carried, target, target));
                assertTrue(carried.isEmpty()); assertTrue(target.isEmpty());
            }
            var sameA = half(season, CosmicCrateSide.LEFT);
            var sameB = half(season, CosmicCrateSide.LEFT);
            assertEquals(CosmicCrateCombinationService.Outcome.REJECTED_INVALID, service.combine(sameA, sameB, sameB));
            assertEquals(1, sameA.getCount()); assertEquals(1, sameB.getCount());
        }
        var spring = half(CosmicCrateSeason.SPRING, CosmicCrateSide.LEFT);
        var summer = half(CosmicCrateSeason.SUMMER, CosmicCrateSide.RIGHT);
        assertEquals(CosmicCrateCombinationService.Outcome.REJECTED_INVALID, service.combine(spring, summer, summer));
        assertEquals(1, spring.getCount()); assertEquals(1, summer.getCount());
    }

    @Test void fullCrateLoreIsPresentationOnlyAndHasExactShape() {
        for (var season : CosmicCrateSeason.values()) {
            assertEquals(9, season.treasure().size()); assertEquals(4, season.bonus().size());
            var lore = CosmicCrateItem.lore(season);
            assertEquals(19, lore.size());
            assertEquals("TREASURE ITEMS", lore.get(1).getString());
            assertEquals("BONUS ITEMS (1)", lore.get(12).getString());
            assertEquals("Ensure you have a lot of free space!", lore.getLast().getString());
            String all = lore.stream().map(component -> component.getString()).reduce("", (a,b) -> a + "\n" + b);
            assertFalse(all.contains("Unlocked by")); assertFalse(all.contains("ADMIN ITEMS"));
            assertFalse(all.contains("COSMETICS")); assertFalse(all.contains("open area"));
        }
        assertTrue(CosmicCrateSeason.SPRING.bonus().stream().noneMatch(line -> line.contains("Memory Chest")));
        for (var season : List.of(CosmicCrateSeason.SUMMER, CosmicCrateSeason.FALL, CosmicCrateSeason.WINTER))
            assertTrue(season.bonus().stream().anyMatch(line -> line.contains("Memory Chest")));
        assertFalse((Object)ModItems.SPRING_COSMIC_CRATE.get()
                instanceof com.cosmicpve.reward.preview.LootPreviewProvider);
    }

    @Test void halfModelsAreComplementaryHalfWidthGeometry() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"), "src/main/resources/assets/cosmicpve/models/item");
        var left = JsonParser.parseString(Files.readString(root.resolve("cosmic_crate_half_left.json")))
                .getAsJsonObject().getAsJsonArray("elements").get(0).getAsJsonObject();
        var right = JsonParser.parseString(Files.readString(root.resolve("cosmic_crate_half_right.json")))
                .getAsJsonObject().getAsJsonArray("elements").get(0).getAsJsonObject();
        assertEquals(0, left.getAsJsonArray("from").get(0).getAsInt());
        assertEquals(8, left.getAsJsonArray("to").get(0).getAsInt());
        assertEquals(8, right.getAsJsonArray("from").get(0).getAsInt());
        assertEquals(16, right.getAsJsonArray("to").get(0).getAsInt());
    }

    @Test void higherLoreOrbsRequireExactSequencePreserveMetadataAndNeverConsumeOnReject() {
        var capacity = new CustomEnchantCapacityService();
        var service = new HigherLoreOrbApplicationService(capacity);
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        armor.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new CustomEnchantMetadata(2,5,3,true,true));
        var armor9 = new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_9_LORE.get());
        assertEquals("Armor Enchantment Orb [9]", armor9.getHoverName().getString());
        assertEquals(HigherLoreOrbApplicationService.Outcome.SUCCESS, service.apply(armor9,armor,armor));
        assertEquals(9,capacity.capacity(armor)); assertTrue(armor9.isEmpty());
        assertTrue(armor.get(ModDataComponents.CUSTOM_ENCHANT_META.get()).whiteScrollProtected());
        assertTrue(armor.get(ModDataComponents.CUSTOM_ENCHANT_META.get()).transmogSorted());
        var direct10 = new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_10_LORE.get());
        assertEquals("Armor Enchantment Orb [10]", direct10.getHoverName().getString());
        assertEquals(HigherLoreOrbApplicationService.Outcome.SUCCESS,service.apply(direct10,armor,armor));
        assertEquals(10,capacity.capacity(armor)); assertEquals(10,capacity.absoluteMaximum(armor));
        var excess = new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_10_LORE.get());
        assertEquals(HigherLoreOrbApplicationService.Outcome.REJECTED_CAPACITY,service.apply(excess,armor,armor));
        assertEquals(1,excess.getCount());

        var weapon = new ItemStack(Items.BOW);
        weapon.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new CustomEnchantMetadata(2,5,5,false,false));
        var weapon12 = new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_12_LORE.get());
        assertEquals("Weapon Enchantment Orb [12]", weapon12.getHoverName().getString());
        assertEquals(HigherLoreOrbApplicationService.Outcome.REJECTED_CAPACITY,service.apply(weapon12,weapon,weapon));
        assertEquals(1,weapon12.getCount());
        var weapon11 = new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_11_LORE.get());
        assertEquals("Weapon Enchantment Orb [11]", weapon11.getHoverName().getString());
        assertEquals(HigherLoreOrbApplicationService.Outcome.SUCCESS,service.apply(weapon11,weapon,weapon));
        assertEquals(HigherLoreOrbApplicationService.Outcome.SUCCESS,service.apply(weapon12,weapon,weapon));
        assertEquals(12,capacity.capacity(weapon)); assertEquals(12,capacity.absoluteMaximum(weapon));
    }

    private static ItemStack half(CosmicCrateSeason season, CosmicCrateSide side) {
        return com.cosmicpve.cosmiccrate.SeasonalCosmicCrates.half(season, side);
    }

    private static void assertMemoryRow(Path file, int weight, int quantity) throws Exception {
        var entries = JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonArray("entries");
        var matches = entries.asList().stream().map(value -> value.getAsJsonObject()).filter(entry ->
                entry.getAsJsonObject("reward").get("type").getAsString().equals("memory_chest")).toList();
        assertEquals(1,matches.size()); var row=matches.getFirst(); assertEquals(weight,row.get("weight").getAsInt());
        assertEquals(quantity,row.has("minimum_quantity")?row.get("minimum_quantity").getAsInt():1);
        assertEquals(quantity,row.has("maximum_quantity")?row.get("maximum_quantity").getAsInt():1);
    }
}
