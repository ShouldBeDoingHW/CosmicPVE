package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialPhase implements StringRepresentable {
    APPRENTICE("apprentice", "Apprentice", 0xE6E032),
    HARDCORE("hardcore", "Hardcore", 0xE6A732),
    DEMONIC("demonic", "Demonic", 0xE65C32);
    public static final Codec<TrialPhase> CODEC = StringRepresentable.fromEnum(TrialPhase::values);
    private final String name;
    private final String displayName;
    private final int color;
    TrialPhase(String name, String displayName, int color) {
        this.name = name; this.displayName = displayName; this.color = color;
    }
    @Override public String getSerializedName() { return name; }
    public String displayName() { return displayName; }
    public int color() { return color; }
}
