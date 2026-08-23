package com.cosmicpve.data.component;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Stable per-stack identity for a generic rarity-bound Unexamined Enchantment Book. */
public record UnexaminedBookData(int dataVersion, CosmicEnchantmentTier tier) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<UnexaminedBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(UnexaminedBookData::dataVersion),
            CosmicEnchantmentTier.CODEC.fieldOf("tier").forGetter(UnexaminedBookData::tier)
    ).apply(instance, UnexaminedBookData::new));

    public UnexaminedBookData {
        if (dataVersion < 1 || tier == null) {
            throw new IllegalArgumentException("Invalid Unexamined Enchantment Book data");
        }
    }
}
