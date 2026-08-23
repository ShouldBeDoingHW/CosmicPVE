package com.cosmicpve.spacechest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record CommittedSpaceChestReward(int slot, List<ItemStack> items, boolean delivered) {
    public static final Codec<CommittedSpaceChestReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 26).fieldOf("slot").forGetter(CommittedSpaceChestReward::slot),
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(CommittedSpaceChestReward::items),
            Codec.BOOL.optionalFieldOf("delivered", false).forGetter(CommittedSpaceChestReward::delivered)
    ).apply(instance, CommittedSpaceChestReward::new));
    public CommittedSpaceChestReward { items = items.stream().map(ItemStack::copy).toList(); }
    public CommittedSpaceChestReward deliveredCopy() { return new CommittedSpaceChestReward(slot, items, true); }
}
