package com.cosmicpve.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.progression.ArmorRecipeProgression;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ProgressionCleanupTest {
    private static final List<String> DISABLED_RECIPES = List.of(
            "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
            "netherite_helmet_smithing", "netherite_chestplate_smithing",
            "netherite_leggings_smithing", "netherite_boots_smithing");
    private static final List<String> TARGET_LOOT_TABLES = List.of(
            "minecraft:chests/ancient_city",
            "minecraft:chests/bastion_treasure",
            "minecraft:chests/end_city_treasure",
            "minecraft:chests/woodland_mansion",
            "minecraft:chests/trial_chambers/reward_rare",
            "minecraft:chests/trial_chambers/reward_ominous_rare");

    @Test void allDiamondAndNetheriteArmorRecipesAreConditionallyDisabled() throws Exception {
        for (String recipe : DISABLED_RECIPES) {
            JsonObject json = resource("resourcepacks/progression_cleanup/data/minecraft/recipe/" + recipe + ".json");
            var conditions = json.getAsJsonArray("neoforge:conditions");
            assertEquals(1, conditions.size(), recipe);
            assertEquals("neoforge:never", conditions.get(0).getAsJsonObject().get("type").getAsString(), recipe);
        }
        assertEquals(DISABLED_RECIPES,
                ArmorRecipeProgression.DISABLED_RECIPES.stream()
                        .map(key -> key.identifier().getPath()).toList());
        assertEquals(List.of(
                        "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots",
                        "diamond_sword", "diamond_pickaxe",
                        "netherite_sword_smithing", "netherite_pickaxe_smithing"),
                ArmorRecipeProgression.REQUIRED_CONTROL_RECIPES.stream()
                        .map(key -> key.identifier().getPath()).toList());
        assertNotNull(resource("resourcepacks/progression_cleanup/pack.mcmeta").getAsJsonObject("pack"));
    }

    @Test void modifierTargetsEveryAuditedChestLootSourceAndNoEncounterEquipmentTable() throws Exception {
        JsonObject json = resource("data/cosmicpve/loot_modifiers/diamond_armor_progression.json");
        assertEquals("cosmicpve:diamond_armor_replacement", json.get("type").getAsString());
        var ids = json.getAsJsonArray("loot_tables").asList().stream().map(value -> value.getAsString()).toList();
        assertEquals(TARGET_LOOT_TABLES, ids);
        assertTrue(ids.stream().noneMatch(id -> id.equals("minecraft:equipment/trial_chamber")));
        JsonObject globals = resource("data/neoforge/loot_modifiers/global_loot_modifiers.json");
        assertTrue(globals.getAsJsonArray("entries").asList().stream()
                .anyMatch(value -> value.getAsString().equals("cosmicpve:diamond_armor_progression")));
    }

    @Test void replacementIsExactlySixtyFiveSimpleThirtyFiveUniqueAndOneBook() {
        assertEquals(CosmicEnchantmentTier.SIMPLE, DiamondArmorLootReplacementService.tierForRoll(0));
        assertEquals(CosmicEnchantmentTier.SIMPLE, DiamondArmorLootReplacementService.tierForRoll(64));
        assertEquals(CosmicEnchantmentTier.UNIQUE, DiamondArmorLootReplacementService.tierForRoll(65));
        assertEquals(CosmicEnchantmentTier.UNIQUE, DiamondArmorLootReplacementService.tierForRoll(99));
        var service = new DiamondArmorLootReplacementService();
        ItemStack replaced = service.replace(new ItemStack(Items.DIAMOND_CHESTPLATE), RandomSource.create(4L));
        assertTrue(replaced.is(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get()));
        assertEquals(1, replaced.getCount());
        assertNotNull(replaced.get(ModDataComponents.UNEXAMINED_BOOK.get()));
        ItemStack iron = new ItemStack(Items.IRON_CHESTPLATE);
        assertTrue(iron == service.replace(iron, RandomSource.create(4L)));
    }

    private JsonObject resource(String path) throws Exception {
        var resources = Collections.list(getClass().getClassLoader().getResources(path));
        var source = resources.stream().filter(url -> url.getProtocol().equals("file")).findFirst().orElseThrow();
        try (var reader = new InputStreamReader(source.openStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
