package com.cosmicpve.content.definition.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialSpawnMarkerRule implements StringRepresentable {
    EMERALD_BLOCK("emerald_block");
    public static final Codec<TrialSpawnMarkerRule> CODEC =
            StringRepresentable.fromEnum(TrialSpawnMarkerRule::values);
    private final String serializedName;
    TrialSpawnMarkerRule(String serializedName) { this.serializedName = serializedName; }
    @Override public String getSerializedName() { return serializedName; }
}
