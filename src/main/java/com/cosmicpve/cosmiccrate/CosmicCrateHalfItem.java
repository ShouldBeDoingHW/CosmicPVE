package com.cosmicpve.cosmiccrate;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CosmicCrateHalfItem extends Item {
    private final CosmicCrateSeason season;
    private final CosmicCrateSide side;
    public CosmicCrateHalfItem(Properties properties, CosmicCrateSeason season, CosmicCrateSide side) {
        super(properties); this.season = season; this.side = side;
    }
    public CosmicCrateSeason season() { return season; }
    public CosmicCrateSide side() { return side; }
    @Override public Component getName(ItemStack stack) {
        return Component.literal(season.displayName() + " Cosmic Crate " + side.displayName() + " Half")
                .withStyle(style -> style.withColor(season.color()).withBold(true));
    }
}
