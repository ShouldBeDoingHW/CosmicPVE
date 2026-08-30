package com.cosmicpve.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Versioned per-player V-Kit levels keyed by stable kit IDs. */
public record VKitProgressionData(int dataVersion, Map<Identifier, Integer> levels) {
    public static final int CURRENT_DATA_VERSION = 1;
    private static final Codec<Map<Identifier, Integer>> LEVELS_CODEC =
            Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, 10));
    public static final com.mojang.serialization.MapCodec<VKitProgressionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(VKitProgressionData::dataVersion),
            LEVELS_CODEC.optionalFieldOf("levels", Map.of()).forGetter(VKitProgressionData::levels)
    ).apply(instance, VKitProgressionData::new));

    public VKitProgressionData {
        levels = Map.copyOf(levels);
    }

    public static VKitProgressionData empty() {
        return new VKitProgressionData(CURRENT_DATA_VERSION, Map.of());
    }

    public int level(Identifier kitId) {
        return levels.getOrDefault(kitId, 0);
    }
}
