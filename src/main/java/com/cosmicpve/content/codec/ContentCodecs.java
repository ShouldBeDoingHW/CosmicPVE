package com.cosmicpve.content.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Locale;

public final class ContentCodecs {
    private ContentCodecs() {}

    public static <E extends Enum<E>> Codec<E> lowerCaseEnum(E[] values, String typeName) {
        return Codec.STRING.comapFlatMap(
                encoded -> Arrays.stream(values)
                        .filter(value -> value.name().equalsIgnoreCase(encoded))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown " + typeName + " '" + encoded + "'")),
                value -> value.name().toLowerCase(Locale.ROOT));
    }
}
