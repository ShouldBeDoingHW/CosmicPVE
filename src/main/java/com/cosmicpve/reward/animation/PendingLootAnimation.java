package com.cosmicpve.reward.animation;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Durable exactly-once obligation for an animated single reward. */
public record PendingLootAnimation(int dataVersion, Optional<ItemStack> reward) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final com.mojang.serialization.MapCodec<PendingLootAnimation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.INT.optionalFieldOf("data_version", CURRENT_DATA_VERSION).forGetter(PendingLootAnimation::dataVersion),
            ItemStack.CODEC.optionalFieldOf("reward").forGetter(PendingLootAnimation::reward)
    ).apply(instance, PendingLootAnimation::new));

    public static PendingLootAnimation empty() { return new PendingLootAnimation(CURRENT_DATA_VERSION, Optional.empty()); }
    public static PendingLootAnimation of(ItemStack reward) { return new PendingLootAnimation(CURRENT_DATA_VERSION, Optional.of(reward.copy())); }
    public boolean valid() { return dataVersion == CURRENT_DATA_VERSION && reward.isPresent() && !reward.orElseThrow().isEmpty(); }
}
