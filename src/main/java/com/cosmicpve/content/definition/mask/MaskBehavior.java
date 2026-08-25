package com.cosmicpve.content.definition.mask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Stable Java behavior identities referenced by reloadable mask presentation data. */
public enum MaskBehavior {
    SANTA, REINDEER, PURGE, PARTY, LOVER, SCARECROW, ZEUS, TURKEY, DRAGON;

    public static final Codec<MaskBehavior> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown mask behavior: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
