package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CustomEnchantMetadata(
        int dataVersion,
        int slotLimit,
        int orbUpgrades,
        boolean whiteScrollProtected) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final int DEFAULT_SLOT_LIMIT = 5;
    public static final CustomEnchantMetadata DEFAULT = new CustomEnchantMetadata(
            CURRENT_DATA_VERSION,
            DEFAULT_SLOT_LIMIT,
            0,
            false);

    public static final Codec<CustomEnchantMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE)
                    .optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(CustomEnchantMetadata::dataVersion),
            Codec.intRange(0, 64)
                    .optionalFieldOf("slot_limit", DEFAULT_SLOT_LIMIT)
                    .forGetter(CustomEnchantMetadata::slotLimit),
            Codec.intRange(0, 64)
                    .optionalFieldOf("orb_upgrades", 0)
                    .forGetter(CustomEnchantMetadata::orbUpgrades),
            Codec.BOOL
                    .optionalFieldOf("white_scroll_protected", false)
                    .forGetter(CustomEnchantMetadata::whiteScrollProtected)
    ).apply(instance, CustomEnchantMetadata::new));

    public CustomEnchantMetadata {
        if (dataVersion < 1) {
            throw new IllegalArgumentException("dataVersion must be positive");
        }
        if (slotLimit < 0 || slotLimit > 64) {
            throw new IllegalArgumentException("slotLimit must be between 0 and 64");
        }
        if (orbUpgrades < 0 || orbUpgrades > 64) {
            throw new IllegalArgumentException("orbUpgrades must be between 0 and 64");
        }
    }
}
