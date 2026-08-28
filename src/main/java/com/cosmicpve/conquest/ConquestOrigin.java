package com.cosmicpve.conquest;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum ConquestOrigin {
    NATURAL, FLARE;

    public static final Codec<ConquestOrigin> CODEC = Codec.STRING.xmap(
            value -> valueOf(value.toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT));
}
