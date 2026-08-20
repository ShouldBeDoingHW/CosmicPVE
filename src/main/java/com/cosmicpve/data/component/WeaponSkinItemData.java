package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Stable, synchronized identity carried by a generic weapon-skin application item. */
public record WeaponSkinItemData(int dataVersion, Identifier skinId) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<WeaponSkinItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(WeaponSkinItemData::dataVersion),
            Identifier.CODEC.fieldOf("skin_id").forGetter(WeaponSkinItemData::skinId)
    ).apply(instance, WeaponSkinItemData::new));

    public WeaponSkinItemData {
        if (dataVersion < 1) throw new IllegalArgumentException("Weapon-skin data version must be positive");
    }
}
