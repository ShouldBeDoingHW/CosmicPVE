package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialPhase implements StringRepresentable {
    APPRENTICE("apprentice"), HARDCORE("hardcore"), DEMONIC("demonic");
    public static final Codec<TrialPhase> CODEC = StringRepresentable.fromEnum(TrialPhase::values);
    private final String name;
    TrialPhase(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
