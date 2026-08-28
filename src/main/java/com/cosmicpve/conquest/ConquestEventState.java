package com.cosmicpve.conquest;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum ConquestEventState {
    ACTIVE, COMPLETED, EXPIRED;

    public static final Codec<ConquestEventState> CODEC = Codec.STRING.xmap(
            value -> valueOf(value.toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT));
}
