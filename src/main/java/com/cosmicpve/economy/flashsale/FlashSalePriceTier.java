package com.cosmicpve.economy.flashsale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum FlashSalePriceTier {
    LOW, MEDIUM, HIGH;

    public static final Codec<FlashSalePriceTier> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown Flash Sale price tier: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));

    public String displayName() { return name().toLowerCase(Locale.ROOT); }
}
