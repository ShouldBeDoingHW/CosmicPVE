package com.cosmicpve.progression;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Enforces and runtime-verifies the armor recipe policy after every server data load. */
public final class ArmorRecipeProgression {
    public static final List<ResourceKey<Recipe<?>>> DISABLED_RECIPES = List.of(
            key("diamond_helmet"), key("diamond_chestplate"), key("diamond_leggings"), key("diamond_boots"),
            key("netherite_helmet_smithing"), key("netherite_chestplate_smithing"),
            key("netherite_leggings_smithing"), key("netherite_boots_smithing"));
    public static final List<ResourceKey<Recipe<?>>> REQUIRED_CONTROL_RECIPES = List.of(
            key("iron_helmet"), key("iron_chestplate"), key("iron_leggings"), key("iron_boots"),
            key("diamond_sword"), key("diamond_pickaxe"),
            key("netherite_sword_smithing"), key("netherite_pickaxe_smithing"));

    private ArmorRecipeProgression() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ArmorRecipeProgression::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ArmorRecipeProgression::onDatapackSync);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        verify(event.getServer().getRecipeManager());
        CosmicPVE.LOGGER.info(
                "Verified {} disabled armor recipes absent and {} progression control recipes present in the live recipe manager",
                DISABLED_RECIPES.size(), REQUIRED_CONTROL_RECIPES.size());
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        verify(event.getPlayerList().getServer().getRecipeManager());
    }

    public static void verify(RecipeManager recipes) {
        var present = DISABLED_RECIPES.stream().filter(key -> recipes.byKey(key).isPresent()).toList();
        if (!present.isEmpty()) {
            throw new IllegalStateException("Forbidden armor recipes remain loaded: " + present);
        }
        var missingControls = REQUIRED_CONTROL_RECIPES.stream()
                .filter(key -> recipes.byKey(key).isEmpty()).toList();
        if (!missingControls.isEmpty()) {
            throw new IllegalStateException("Unrelated progression recipes were removed: " + missingControls);
        }
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace(path));
    }
}
