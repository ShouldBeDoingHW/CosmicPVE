package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BanknoteData(int dataVersion, long valueCents) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<BanknoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(BanknoteData::dataVersion),
            Codec.LONG.validate(value -> value > 0 ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "value_cents must be positive"))
                    .fieldOf("value_cents").forGetter(BanknoteData::valueCents)
    ).apply(instance, BanknoteData::new));

    public BanknoteData {
        if (dataVersion < 1 || valueCents <= 0) throw new IllegalArgumentException("Banknote value must be positive");
    }
}
