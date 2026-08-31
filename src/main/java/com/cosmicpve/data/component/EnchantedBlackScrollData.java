package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Versioned fixed returned-book Success Rate for the selection-based scroll. */
public record EnchantedBlackScrollData(int dataVersion, int returnedSuccessRate) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<EnchantedBlackScrollData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(EnchantedBlackScrollData::dataVersion),
            Codec.intRange(1, 100).fieldOf("returned_success_rate")
                    .forGetter(EnchantedBlackScrollData::returnedSuccessRate)
    ).apply(instance, EnchantedBlackScrollData::new));

    public EnchantedBlackScrollData {
        if (dataVersion < 1 || returnedSuccessRate < 1 || returnedSuccessRate > 100) {
            throw new IllegalArgumentException("Enchanted Black Scroll returned Success Rate must be in [1,100]");
        }
    }
}
