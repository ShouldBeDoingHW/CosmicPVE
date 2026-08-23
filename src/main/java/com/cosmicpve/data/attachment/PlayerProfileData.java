package com.cosmicpve.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlayerProfileData(int dataVersion, long moneyCents) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final MapCodec<PlayerProfileData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE)
                    .optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(PlayerProfileData::dataVersion),
            Codec.LONG.validate(value -> value >= 0 ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "money_cents must not be negative"))
                    .optionalFieldOf("money_cents", 0L)
                    .forGetter(PlayerProfileData::moneyCents)
    ).apply(instance, PlayerProfileData::new));

    public static PlayerProfileData createDefault() {
        return new PlayerProfileData(CURRENT_DATA_VERSION, 0L);
    }

    public PlayerProfileData {
        if (dataVersion < 1) {
            throw new IllegalArgumentException("dataVersion must be positive");
        }
        if (moneyCents < 0) throw new IllegalArgumentException("moneyCents must not be negative");
    }
}
