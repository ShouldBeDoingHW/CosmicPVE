package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record MobSpawnerData(int version, Identifier entityTypeId) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<MobSpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("version").forGetter(MobSpawnerData::version),
            Identifier.CODEC.fieldOf("entity_type").forGetter(MobSpawnerData::entityTypeId)
    ).apply(instance, MobSpawnerData::new));

    public boolean isCurrent() {
        return version == CURRENT_DATA_VERSION;
    }
}
