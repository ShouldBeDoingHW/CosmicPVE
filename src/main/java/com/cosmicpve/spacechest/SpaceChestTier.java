package com.cosmicpve.spacechest;

import com.cosmicpve.CosmicPVE;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import net.minecraft.resources.Identifier;

public enum SpaceChestTier {
    ULTIMATE(0xFFFF55), LEGENDARY(0xFFAA00), MASTERY(0xAA0000);

    public static final Codec<SpaceChestTier> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown Space Chest tier: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));

    private final int color;
    SpaceChestTier(int color) { this.color = color; }
    public int color() { return color; }
    public Identifier rewardTableId() { return CosmicPVE.id("space_chest/" + name().toLowerCase(Locale.ROOT)); }
    public String serializedName() { return name().toLowerCase(Locale.ROOT); }
}
