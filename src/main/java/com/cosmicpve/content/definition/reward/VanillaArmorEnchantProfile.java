package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Fixed vanilla enchantments added alongside the generated Cosmic armor enchantments. */
public enum VanillaArmorEnchantProfile {
    NONE, PROTECTION_UNBREAKING, PROTECTION_UNBREAKING_MENDING;

    public static final Codec<VanillaArmorEnchantProfile> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown vanilla armor enchant profile: " + value);
        }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
