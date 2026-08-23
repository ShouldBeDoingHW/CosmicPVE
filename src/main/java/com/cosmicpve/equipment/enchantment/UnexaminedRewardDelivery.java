package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

/** Resolves the held-stack transformation before the item-use pipeline writes the hand slot. */
public final class UnexaminedRewardDelivery {
    private UnexaminedRewardDelivery() {}

    public static ItemStack deliver(ItemStack source, ItemStack reward, Consumer<ItemStack> inventoryOrDrop) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(reward, "reward");
        Objects.requireNonNull(inventoryOrDrop, "inventoryOrDrop");
        if (source.isEmpty() || reward.isEmpty()) {
            throw new IllegalArgumentException("Unexamined Book delivery requires non-empty source and reward stacks");
        }

        source.shrink(1);
        if (source.isEmpty()) {
            return reward;
        }

        inventoryOrDrop.accept(reward);
        return source;
    }
}
