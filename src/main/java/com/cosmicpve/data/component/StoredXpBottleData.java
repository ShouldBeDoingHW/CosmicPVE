package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent, fixed-point-free XP payload for one salvaged equipment item. */
public record StoredXpBottleData(int dataVersion, long storedXp) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<StoredXpBottleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(StoredXpBottleData::dataVersion),
            Codec.LONG.validate(value -> value > 0 ? DataResult.success(value)
                    : DataResult.error(() -> "Stored XP must be positive"))
                    .fieldOf("stored_xp").forGetter(StoredXpBottleData::storedXp)
    ).apply(instance, StoredXpBottleData::new));

    public StoredXpBottleData {
        if (dataVersion < 1 || storedXp < 1) throw new IllegalArgumentException("Stored XP must be positive");
    }
}
