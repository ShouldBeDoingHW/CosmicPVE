package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TrialTrinketData(int dataVersion, TrialTrinketType type, int value) {
    public static final int DATA_VERSION = 1;
    private static final Codec<TrialTrinketData> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TrialTrinketData::dataVersion),
            TrialTrinketType.CODEC.fieldOf("type").forGetter(TrialTrinketData::type),
            Codec.INT.fieldOf("value").forGetter(TrialTrinketData::value)
    ).apply(instance, TrialTrinketData::new));
    public static final Codec<TrialTrinketData> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "Invalid Trial Trinket type/value"));

    public TrialTrinketData(TrialTrinketType type, int value) { this(DATA_VERSION, type, value); }
    public boolean valid() { return dataVersion == DATA_VERSION && type != null && type.validValue(value); }
}
