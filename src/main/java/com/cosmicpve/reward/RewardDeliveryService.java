package com.cosmicpve.reward;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RewardDeliveryService {
    public void deliver(ServerPlayer player, List<ItemStack> rewards) {
        for (ItemStack reward : rewards) {
            if (!reward.isEmpty()) player.getInventory().placeItemBackInInventory(reward.copy());
        }
    }
}
