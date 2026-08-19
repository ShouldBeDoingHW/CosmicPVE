package com.cosmicpve.content.definition.stack;

import com.cosmicpve.content.codec.ContentCodecs;
import com.mojang.serialization.Codec;

public enum StackRefreshPolicy {
    REFRESH_ALL,
    REFRESH_ONE,
    INDEPENDENT,
    FIXED;

    public static final Codec<StackRefreshPolicy> CODEC =
            ContentCodecs.lowerCaseEnum(values(), "stack refresh policy");
}
