package com.cosmicpve.trial.persistence;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public record TrialPlayerState(TrialSnapshotPhase phase, Optional<TrialOutsideSnapshot> snapshot,
        List<ItemStack> pendingRewards, boolean rewardRestorePrepared, long pendingFame) {
    public static final MapCodec<TrialPlayerState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TrialSnapshotPhase.CODEC.fieldOf("phase").forGetter(TrialPlayerState::phase),
            TrialOutsideSnapshot.CODEC.optionalFieldOf("snapshot").forGetter(TrialPlayerState::snapshot),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("pending_rewards", List.of())
                    .forGetter(TrialPlayerState::pendingRewards),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("reward_restore_prepared", false)
                    .forGetter(TrialPlayerState::rewardRestorePrepared),
            com.mojang.serialization.Codec.LONG.optionalFieldOf("pending_fame", 0L).forGetter(TrialPlayerState::pendingFame)
    ).apply(instance, TrialPlayerState::new));

    public TrialPlayerState { pendingRewards = pendingRewards.stream().map(ItemStack::copy).toList(); if (pendingFame < 0) throw new IllegalArgumentException("pendingFame cannot be negative"); }
    public static TrialPlayerState restored() { return new TrialPlayerState(TrialSnapshotPhase.RESTORED, Optional.empty(), List.of(), false, 0L); }
    public static TrialPlayerState committed(TrialOutsideSnapshot snapshot) {
        return new TrialPlayerState(TrialSnapshotPhase.SNAPSHOT_COMMITTED, Optional.of(snapshot), List.of(), false, 0L);
    }
    public TrialPlayerState inTrial() { return new TrialPlayerState(TrialSnapshotPhase.IN_TRIAL, snapshot, pendingRewards, rewardRestorePrepared, pendingFame); }
    public TrialPlayerState withPreparedRewards(List<ItemStack> rewards, long fame) {
        return new TrialPlayerState(phase, snapshot, rewards, true, fame);
    }
    public TrialPlayerState withPreparedRewards(List<ItemStack> rewards) { return withPreparedRewards(rewards, 0L); }
    public TrialPlayerState withPendingRewards(List<ItemStack> rewards) { return withPreparedRewards(rewards, 0L); }
}
