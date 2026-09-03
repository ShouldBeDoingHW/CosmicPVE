package com.cosmicpve.reward.animation;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Durable exactly-once obligation for an animated single reward. */
public record PendingLootAnimation(int dataVersion, Optional<ItemStack> reward, java.util.List<ItemStack> rewards) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final com.mojang.serialization.MapCodec<PendingLootAnimation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.INT.optionalFieldOf("data_version", CURRENT_DATA_VERSION).forGetter(PendingLootAnimation::dataVersion),
            ItemStack.CODEC.optionalFieldOf("reward").forGetter(PendingLootAnimation::reward),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("rewards", java.util.List.of()).forGetter(PendingLootAnimation::rewards)
    ).apply(instance, PendingLootAnimation::new));

    public PendingLootAnimation { rewards = rewards.stream().map(ItemStack::copy).toList(); }
    public static PendingLootAnimation empty() { return new PendingLootAnimation(CURRENT_DATA_VERSION, Optional.empty(), java.util.List.of()); }
    public static PendingLootAnimation of(ItemStack reward) { return new PendingLootAnimation(CURRENT_DATA_VERSION, Optional.of(reward.copy()), java.util.List.of()); }
    public static PendingLootAnimation of(java.util.List<ItemStack> rewards) { return new PendingLootAnimation(CURRENT_DATA_VERSION, Optional.empty(), rewards); }
    public java.util.List<ItemStack> allRewards() {
        if (!rewards.isEmpty()) return rewards.stream().map(ItemStack::copy).toList();
        return reward.map(value -> java.util.List.of(value.copy())).orElse(java.util.List.of());
    }
    public boolean valid() { return dataVersion == CURRENT_DATA_VERSION && !allRewards().isEmpty()
            && allRewards().stream().noneMatch(ItemStack::isEmpty); }
}
