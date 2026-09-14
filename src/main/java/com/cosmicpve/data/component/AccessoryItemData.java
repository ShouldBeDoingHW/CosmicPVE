package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Canonical identity carried by a loose accessory item. */
public record AccessoryItemData(int dataVersion, AccessorySlot slot, Identifier accessoryId) {
    public static final int DATA_VERSION = 1;
    private static final Codec<AccessoryItemData> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(AccessoryItemData::dataVersion),
            AccessorySlot.CODEC.fieldOf("slot").forGetter(AccessoryItemData::slot),
            Identifier.CODEC.fieldOf("accessory_id").forGetter(AccessoryItemData::accessoryId)
    ).apply(instance, AccessoryItemData::new));
    public static final Codec<AccessoryItemData> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? DataResult.success(value) : DataResult.error(() -> "Invalid accessory item identity"));
    public boolean valid() { return dataVersion == DATA_VERSION && slot != null && accessoryId != null; }
}
