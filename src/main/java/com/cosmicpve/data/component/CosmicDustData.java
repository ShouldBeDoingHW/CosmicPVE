package com.cosmicpve.data.component;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CosmicDustData(int dataVersion, CosmicEnchantmentTier tier) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<CosmicDustData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(CosmicDustData::dataVersion),
            CosmicEnchantmentTier.CODEC.fieldOf("tier").forGetter(CosmicDustData::tier)
    ).apply(instance, CosmicDustData::new));

    public CosmicDustData {
        if (dataVersion < 1) throw new IllegalArgumentException("Invalid Cosmic Dust data version");
    }
}
