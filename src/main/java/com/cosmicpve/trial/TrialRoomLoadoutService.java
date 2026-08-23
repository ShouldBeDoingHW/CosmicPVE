package com.cosmicpve.trial;

import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

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
    public void applyRaidingRainbow(ServerPlayer player) {
        clear(player);
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        var unbreaking = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
        EnchantmentHelper.updateEnchantments(sword, mutable -> mutable.set(unbreaking, 3));
        player.getInventory().setItem(0, sword);
        player.getInventory().setItem(1, new ItemStack(Items.COOKED_PORKCHOP, 16));
        player.getInventory().setItem(2, new ItemStack(Items.ENDER_PEARL, 2));
        player.getInventory().setSelectedSlot(0);
    }
    public void applyCircuitCircus(ServerPlayer player) {
        clear(player);
        ItemStack bow = new ItemStack(Items.BOW);
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(bow, mutable -> mutable.set(registry.getOrThrow(Enchantments.INFINITY), 1));
        player.getInventory().setItem(0, bow);
        player.getInventory().setItem(1, new ItemStack(Items.ARROW));
        player.getInventory().setSelectedSlot(0);
    }
}
