package com.cosmicpve.spacechest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SpaceChestSession(int dataVersion, SpaceChestTier tier, SpaceChestPhase phase,
        List<Integer> selectedSlots, List<CommittedSpaceChestReward> committedRewards) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<SpaceChestSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(SpaceChestSession::dataVersion),
            SpaceChestTier.CODEC.fieldOf("tier").forGetter(SpaceChestSession::tier),
            SpaceChestPhase.CODEC.fieldOf("phase").forGetter(SpaceChestSession::phase),
            Codec.intRange(0, 26).listOf().optionalFieldOf("selected_slots", List.of())
                    .forGetter(SpaceChestSession::selectedSlots),
            CommittedSpaceChestReward.CODEC.listOf().optionalFieldOf("committed_rewards", List.of())
                    .forGetter(SpaceChestSession::committedRewards)
    ).apply(instance, SpaceChestSession::new));

    public SpaceChestSession {
        selectedSlots = List.copyOf(selectedSlots);
        committedRewards = List.copyOf(committedRewards);
        if (selectedSlots.size() > 5 || selectedSlots.stream().distinct().count() != selectedSlots.size())
            throw new IllegalArgumentException("Space Chest selection must contain at most five distinct slots");
        if (phase == SpaceChestPhase.COMMITTED && (selectedSlots.size() != 5 || committedRewards.size() != 5))
            throw new IllegalArgumentException("Committed Space Chest sessions require five selected reward bundles");
    }

    public static SpaceChestSession selecting(SpaceChestTier tier) {
        return new SpaceChestSession(CURRENT_DATA_VERSION, tier, SpaceChestPhase.SELECTING, List.of(), List.of());
    }
}
