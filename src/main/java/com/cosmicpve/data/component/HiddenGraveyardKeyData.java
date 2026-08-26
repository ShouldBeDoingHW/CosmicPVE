package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record HiddenGraveyardKeyData(int dataVersion, UUID sessionId, UUID attemptId, int sequence) {
    public static final int DATA_VERSION = 1;
    private static final Codec<HiddenGraveyardKeyData> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(HiddenGraveyardKeyData::dataVersion),
            UUIDUtil.CODEC.fieldOf("session_id").forGetter(HiddenGraveyardKeyData::sessionId),
            UUIDUtil.CODEC.fieldOf("attempt_id").forGetter(HiddenGraveyardKeyData::attemptId),
            Codec.intRange(1, 3).fieldOf("sequence").forGetter(HiddenGraveyardKeyData::sequence)
    ).apply(instance, HiddenGraveyardKeyData::new));
    public static final Codec<HiddenGraveyardKeyData> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "Invalid Hidden Graveyard key data"));
    public HiddenGraveyardKeyData(UUID session, UUID attempt, int sequence) {
        this(DATA_VERSION, session, attempt, sequence);
    }
    public HiddenGraveyardKeyData {
        if (dataVersion != DATA_VERSION || sessionId == null || attemptId == null || sequence < 1 || sequence > 3)
            throw new IllegalArgumentException("Invalid Hidden Graveyard key data");
    }
    public boolean valid() { return dataVersion == DATA_VERSION && sessionId != null && attemptId != null
            && sequence >= 1 && sequence <= 3; }
}
