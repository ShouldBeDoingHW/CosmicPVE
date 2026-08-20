package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Versioned per-stack configuration; the rate belongs to the returned book, not extraction. */
public record BlackScrollData(int dataVersion, int returnedSuccessRate) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<BlackScrollData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(BlackScrollData::dataVersion),
            Codec.intRange(1, 100).fieldOf("returned_success_rate").forGetter(BlackScrollData::returnedSuccessRate)
    ).apply(instance, BlackScrollData::new));

    public BlackScrollData {
        if (dataVersion < 1 || returnedSuccessRate < 1 || returnedSuccessRate > 100) {
            throw new IllegalArgumentException("Black Scroll returned Success Rate must be between 1 and 100");
        }
    }
}
