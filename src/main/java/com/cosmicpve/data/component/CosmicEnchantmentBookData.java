package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record CosmicEnchantmentBookData(
        int dataVersion, Identifier enchantmentId, int level, int successRate, int destroyRate) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<CosmicEnchantmentBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(CosmicEnchantmentBookData::dataVersion),
            Identifier.CODEC.fieldOf("enchantment_id").forGetter(CosmicEnchantmentBookData::enchantmentId),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("level").forGetter(CosmicEnchantmentBookData::level),
            Codec.intRange(1, 100).fieldOf("success_rate").forGetter(CosmicEnchantmentBookData::successRate),
            Codec.intRange(1, 100).fieldOf("destroy_rate").forGetter(CosmicEnchantmentBookData::destroyRate)
    ).apply(instance, CosmicEnchantmentBookData::new));

    public CosmicEnchantmentBookData {
        if (dataVersion < 1 || level < 1 || successRate < 1 || successRate > 100
                || destroyRate < 1 || destroyRate > 100) {
            throw new IllegalArgumentException("Invalid Cosmic Enchantment Book data");
        }
    }
}
