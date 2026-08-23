package com.cosmicpve.trial.persistence;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TrialSnapshotPhase implements StringRepresentable {
    SNAPSHOT_COMMITTED("snapshot_committed"), IN_TRIAL("in_trial"), RESTORED("restored");
    public static final Codec<TrialSnapshotPhase> CODEC = StringRepresentable.fromEnum(TrialSnapshotPhase::values);
    private final String name;
    TrialSnapshotPhase(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
