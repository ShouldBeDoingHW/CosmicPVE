package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum EnchantmentLevelMode {
    MAXIMUM, RANDOM_VALID;
    public static final Codec<EnchantmentLevelMode> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown level mode: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
