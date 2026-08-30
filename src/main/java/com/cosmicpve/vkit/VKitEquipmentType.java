package com.cosmicpve.vkit;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum VKitEquipmentType implements StringRepresentable {
    ARMOR("armor"),
    WEAPON("weapon");

    public static final Codec<VKitEquipmentType> CODEC = StringRepresentable.fromEnum(VKitEquipmentType::values);
    private final String serializedName;

    VKitEquipmentType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
