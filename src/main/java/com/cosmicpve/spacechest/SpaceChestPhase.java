package com.cosmicpve.spacechest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum SpaceChestPhase {
    SELECTING, COMMITTED;
    public static final Codec<SpaceChestPhase> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown Space Chest phase: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
