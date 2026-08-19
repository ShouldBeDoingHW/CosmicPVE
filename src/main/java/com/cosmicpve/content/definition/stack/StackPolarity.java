package com.cosmicpve.content.definition.stack;

import com.cosmicpve.content.codec.ContentCodecs;
import com.mojang.serialization.Codec;

public enum StackPolarity {
    POSITIVE,
    NEGATIVE;

    public static final Codec<StackPolarity> CODEC =
            ContentCodecs.lowerCaseEnum(values(), "stack polarity");
}
