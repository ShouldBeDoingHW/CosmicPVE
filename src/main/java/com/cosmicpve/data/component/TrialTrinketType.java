package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialTrinketType implements StringRepresentable {
    TIME("time"), SKIP("skip"), INSURANCE("insurance");

    public static final Codec<TrialTrinketType> CODEC = StringRepresentable.fromEnum(TrialTrinketType::values);
    private final String serializedName;

    TrialTrinketType(String serializedName) { this.serializedName = serializedName; }
    @Override public String getSerializedName() { return serializedName; }

    public boolean validValue(int value) {
        return switch (this) {
            case TIME -> value == 1 || value == 3 || value == 5;
            case SKIP, INSURANCE -> value >= 1 && value <= 3;
        };
    }

    public int presentationColor() {
        return switch (this) {
            case SKIP -> 0x2BC2B8;
            case TIME -> 0x0A5751;
            case INSURANCE -> 0x0A5721;
        };
    }
}
