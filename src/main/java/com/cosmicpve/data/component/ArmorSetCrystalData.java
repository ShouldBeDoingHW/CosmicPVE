package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ArmorSetCrystalData(int dataVersion, ArmorSetIdentity identity, int successRate) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<ArmorSetCrystalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(ArmorSetCrystalData::dataVersion),
            ArmorSetIdentity.CODEC.fieldOf("identity").forGetter(ArmorSetCrystalData::identity),
            Codec.intRange(1, 100).fieldOf("success_rate").forGetter(ArmorSetCrystalData::successRate)
    ).apply(instance, ArmorSetCrystalData::new));

    public ArmorSetCrystalData {
        if (dataVersion < 1 || successRate < 1 || successRate > 100) {
            throw new IllegalArgumentException("Armor Set Crystal success rate must be between 1 and 100");
        }
    }
}
