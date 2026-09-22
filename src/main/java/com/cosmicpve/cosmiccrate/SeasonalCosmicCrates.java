package com.cosmicpve.cosmiccrate;

import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SeasonalCosmicCrates {
    private SeasonalCosmicCrates() {}
    public static ItemStack half(CosmicCrateSeason season, CosmicCrateSide side) { return new ItemStack(halfItem(season, side)); }
    public static ItemStack complete(CosmicCrateSeason season) { return new ItemStack(completeItem(season)); }
    public static Item halfItem(CosmicCrateSeason season, CosmicCrateSide side) {
        return switch (season) {
            case SPRING -> side == CosmicCrateSide.LEFT ? ModItems.SPRING_COSMIC_CRATE_LEFT_HALF.get() : ModItems.SPRING_COSMIC_CRATE_RIGHT_HALF.get();
            case SUMMER -> side == CosmicCrateSide.LEFT ? ModItems.SUMMER_COSMIC_CRATE_LEFT_HALF.get() : ModItems.SUMMER_COSMIC_CRATE_RIGHT_HALF.get();
            case FALL -> side == CosmicCrateSide.LEFT ? ModItems.FALL_COSMIC_CRATE_LEFT_HALF.get() : ModItems.FALL_COSMIC_CRATE_RIGHT_HALF.get();
            case WINTER -> side == CosmicCrateSide.LEFT ? ModItems.WINTER_COSMIC_CRATE_LEFT_HALF.get() : ModItems.WINTER_COSMIC_CRATE_RIGHT_HALF.get();
        };
    }
    public static Item completeItem(CosmicCrateSeason season) {
        return switch (season) {
            case SPRING -> ModItems.SPRING_COSMIC_CRATE.get();
            case SUMMER -> ModItems.SUMMER_COSMIC_CRATE.get();
            case FALL -> ModItems.FALL_COSMIC_CRATE.get();
            case WINTER -> ModItems.WINTER_COSMIC_CRATE.get();
        };
    }
}
