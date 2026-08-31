package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Auditable source-scoped exception to the ordinary Cosmic Book rate policy. */
public record CosmicBookRateOverride(int dataVersion, Identifier sourceId) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<CosmicBookRateOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(CosmicBookRateOverride::dataVersion),
            Identifier.CODEC.fieldOf("source_id").forGetter(CosmicBookRateOverride::sourceId)
    ).apply(instance, CosmicBookRateOverride::new));
}
