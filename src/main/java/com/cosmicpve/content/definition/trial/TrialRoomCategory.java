package com.cosmicpve.content.definition.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialRoomCategory implements StringRepresentable {
    DECISION("decision"), DEVELOPMENT("development"), APPRENTICE("apprentice"),
    HARDCORE("hardcore"), IMPOSSIBLE("impossible"), DEMONIC("demonic");

    public static final Codec<TrialRoomCategory> CODEC = StringRepresentable.fromEnum(TrialRoomCategory::values);
    private final String serializedName;
    TrialRoomCategory(String serializedName) { this.serializedName = serializedName; }
    @Override public String getSerializedName() { return serializedName; }
}
