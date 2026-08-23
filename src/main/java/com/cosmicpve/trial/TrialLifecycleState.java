package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialLifecycleState implements StringRepresentable {
    JOINING("joining"), DECISION("decision"), ROOM_INTRO("room_intro"),
    ROOM_ACTIVE("room_active"), ENDING("ending"), CLOSED("closed");
    public static final Codec<TrialLifecycleState> CODEC = StringRepresentable.fromEnum(TrialLifecycleState::values);
    private final String name;
    TrialLifecycleState(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
