package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Active skin identity plus the exact item-model component restored when the skin is removed. */
public record WeaponSkinIdentity(int dataVersion, Identifier skinId, Optional<Identifier> previousItemModel) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<WeaponSkinIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(WeaponSkinIdentity::dataVersion),
            Identifier.CODEC.fieldOf("skin_id").forGetter(WeaponSkinIdentity::skinId),
            Identifier.CODEC.optionalFieldOf("previous_item_model").forGetter(WeaponSkinIdentity::previousItemModel)
    ).apply(instance, WeaponSkinIdentity::new));

    public WeaponSkinIdentity {
        previousItemModel = previousItemModel == null ? Optional.empty() : previousItemModel;
        if (dataVersion < 1) throw new IllegalArgumentException("Weapon-skin identity version must be positive");
    }
}
