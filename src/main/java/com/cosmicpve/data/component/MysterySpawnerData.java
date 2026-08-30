package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MysterySpawnerData(int dataVersion, MysterySpawnerTier tier) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<MysterySpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("data_version").forGetter(MysterySpawnerData::dataVersion),
            MysterySpawnerTier.CODEC.fieldOf("tier").forGetter(MysterySpawnerData::tier)
    ).apply(instance, MysterySpawnerData::new));
    public boolean isCurrent() { return dataVersion == CURRENT_DATA_VERSION; }
}
