package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Per-stack success and destruction chances shared by both Orb item types. */
public record EnchantmentOrbData(int dataVersion, int successRate, int destroyRate) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<EnchantmentOrbData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(EnchantmentOrbData::dataVersion),
            Codec.intRange(1, 100).fieldOf("success_rate").forGetter(EnchantmentOrbData::successRate),
            Codec.intRange(1, 100).fieldOf("destroy_rate").forGetter(EnchantmentOrbData::destroyRate)
    ).apply(instance, EnchantmentOrbData::new));

    public EnchantmentOrbData {
        if (dataVersion < 1) throw new IllegalArgumentException("dataVersion must be positive");
        if (successRate < 1 || successRate > 100) throw new IllegalArgumentException("successRate must be between 1 and 100");
        if (destroyRate < 1 || destroyRate > 100) throw new IllegalArgumentException("destroyRate must be between 1 and 100");
    }
}
