package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Item-application chance for a concrete accessory socket; deliberately has no destroy rate. */
public record AccessorySocketData(int dataVersion, AccessorySlot slot, int successRate) {
    public static final int DATA_VERSION = 1;
    private static final Codec<AccessorySocketData> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(AccessorySocketData::dataVersion),
            AccessorySlot.CODEC.fieldOf("slot").forGetter(AccessorySocketData::slot),
            Codec.INT.fieldOf("success_rate").forGetter(AccessorySocketData::successRate)
    ).apply(instance, AccessorySocketData::new));
    public static final Codec<AccessorySocketData> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? DataResult.success(value) : DataResult.error(() -> "Accessory socket success rate must be 1-100"));
    public boolean valid() { return dataVersion == DATA_VERSION && slot != null && successRate >= 1 && successRate <= 100; }
}
