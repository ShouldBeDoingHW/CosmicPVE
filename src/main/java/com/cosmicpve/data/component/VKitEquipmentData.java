package com.cosmicpve.data.component;

import com.cosmicpve.vkit.VKitEquipmentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Persistent provenance for a generated V-Kit equipment roll. */
public record VKitEquipmentData(int dataVersion, Identifier kitId, int kitLevel, VKitEquipmentType equipmentType) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<VKitEquipmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("data_version").forGetter(VKitEquipmentData::dataVersion),
            Identifier.CODEC.fieldOf("kit_id").forGetter(VKitEquipmentData::kitId),
            Codec.intRange(1, 10).fieldOf("kit_level").forGetter(VKitEquipmentData::kitLevel),
            VKitEquipmentType.CODEC.fieldOf("equipment_type").forGetter(VKitEquipmentData::equipmentType)
    ).apply(instance, VKitEquipmentData::new));
}
