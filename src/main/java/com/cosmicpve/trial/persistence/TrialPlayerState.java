package com.cosmicpve.trial.persistence;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record TrialPlayerState(TrialSnapshotPhase phase, Optional<TrialOutsideSnapshot> snapshot) {
    public static final MapCodec<TrialPlayerState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TrialSnapshotPhase.CODEC.fieldOf("phase").forGetter(TrialPlayerState::phase),
            TrialOutsideSnapshot.CODEC.optionalFieldOf("snapshot").forGetter(TrialPlayerState::snapshot)
    ).apply(instance, TrialPlayerState::new));

    public static TrialPlayerState restored() { return new TrialPlayerState(TrialSnapshotPhase.RESTORED, Optional.empty()); }
    public static TrialPlayerState committed(TrialOutsideSnapshot snapshot) {
        return new TrialPlayerState(TrialSnapshotPhase.SNAPSHOT_COMMITTED, Optional.of(snapshot));
    }
    public TrialPlayerState inTrial() { return new TrialPlayerState(TrialSnapshotPhase.IN_TRIAL, snapshot); }
}
