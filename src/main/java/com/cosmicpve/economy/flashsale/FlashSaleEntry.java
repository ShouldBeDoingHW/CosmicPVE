package com.cosmicpve.economy.flashsale;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Stable row identity and exact price tiers; rows are equal-weight at selection time. */
public record FlashSaleEntry(String id, Component displayName, int quantity,
        long lowPrice, long mediumPrice, long highPrice, boolean productionSelectable,
        RewardFactory rewardFactory) {
    public FlashSaleEntry {
        if (id == null || id.isBlank() || quantity < 1 || lowPrice < 1 || mediumPrice < 1 || highPrice < 1)
            throw new IllegalArgumentException("Invalid Flash Sale entry");
    }

    public long price(FlashSalePriceTier tier) {
        return switch (tier) { case LOW -> lowPrice; case MEDIUM -> mediumPrice; case HIGH -> highPrice; };
    }

    public Optional<List<ItemStack>> create(RandomSource random) {
        return rewardFactory.create(random).map(stacks -> stacks.stream().map(ItemStack::copy).toList());
    }

    @FunctionalInterface public interface RewardFactory {
        Optional<List<ItemStack>> create(RandomSource random);
    }
}
