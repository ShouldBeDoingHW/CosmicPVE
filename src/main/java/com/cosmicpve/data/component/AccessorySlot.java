package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Stable accessory-slot identity. BELT is reserved data vocabulary, not registered content yet. */
public enum AccessorySlot {
    AMULET,
    BELT;

    public static final Codec<AccessorySlot> CODEC = Codec.STRING.comapFlatMap(value -> {
        try {
            return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown accessory slot: " + value);
        }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
