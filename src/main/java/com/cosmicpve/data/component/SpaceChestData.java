package com.cosmicpve.data.component;

import com.cosmicpve.spacechest.SpaceChestTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpaceChestData(int dataVersion, SpaceChestTier tier) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<SpaceChestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(SpaceChestData::dataVersion),
            SpaceChestTier.CODEC.fieldOf("tier").forGetter(SpaceChestData::tier)
    ).apply(instance, SpaceChestData::new));

    public SpaceChestData(SpaceChestTier tier) { this(CURRENT_DATA_VERSION, tier); }
}
