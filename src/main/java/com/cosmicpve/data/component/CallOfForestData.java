package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Typed duration carried by a Call of the Forest item. */
public record CallOfForestData(int version, int minutes) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<CallOfForestData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("version").forGetter(CallOfForestData::version),
            Codec.intRange(1, 30).fieldOf("minutes").forGetter(CallOfForestData::minutes)
    ).apply(i, CallOfForestData::new));
    public CallOfForestData {
        if (version != CURRENT_DATA_VERSION || minutes < 1 || minutes > 30)
            throw new IllegalArgumentException("Invalid Call of the Forest data");
    }
}
