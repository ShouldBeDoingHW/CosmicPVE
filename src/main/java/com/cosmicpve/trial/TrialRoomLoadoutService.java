package com.cosmicpve.trial;

import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TrialRoomLoadoutService {
    private final TrialInventoryTransactionService inventories;
    public TrialRoomLoadoutService(TrialInventoryTransactionService inventories) { this.inventories = inventories; }
    public void clear(ServerPlayer player) { inventories.clearTrialInventory(player); }
    public void applyDevelopment(ServerPlayer player) {
        clear(player);
        ItemStack marker = new ItemStack(Items.STICK);
        marker.set(DataComponents.CUSTOM_NAME, Component.literal("Development Trial Item"));
        player.getInventory().setItem(0, marker);
        player.getInventory().setSelectedSlot(0);
    }
}
