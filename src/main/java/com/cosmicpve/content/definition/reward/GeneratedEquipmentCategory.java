package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum GeneratedEquipmentCategory {
    RANDOM_IRON_ARMOR_PIECE;
    public static final Codec<GeneratedEquipmentCategory> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown equipment category: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
