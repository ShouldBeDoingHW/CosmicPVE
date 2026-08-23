package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialDecision implements StringRepresentable {
    UNDECIDED("undecided"), DEAL("deal"), NO_DEAL("no_deal");
    public static final Codec<TrialDecision> CODEC = StringRepresentable.fromEnum(TrialDecision::values);
    private final String name;
    TrialDecision(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
