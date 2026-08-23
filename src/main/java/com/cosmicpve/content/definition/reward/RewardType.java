package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum RewardType {
    STATIC_ITEM, BANKNOTE, COSMIC_BOOK, BLACK_SCROLL, ARMOR_ORB, WEAPON_ORB, MOB_SPAWNER, GENERATED_EQUIPMENT;

    public static final Codec<RewardType> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown reward type: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
