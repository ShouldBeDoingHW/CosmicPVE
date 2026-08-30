package com.cosmicpve.personalvault;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

/** Server-side container facade that publishes an immutable attachment update on every mutation. */
final class PersonalVaultContainer implements Container {
    private final ServerPlayer owner;
    private final long vaultNumber;
    private final int size;
    private final NonNullList<ItemStack> items;
    PersonalVaultContainer(ServerPlayer owner, long vaultNumber, int rows) {
        this.owner = owner; this.vaultNumber = vaultNumber; this.size = rows * 9;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int slot = 0; slot < size; slot++) items.set(slot,
                PersonalVaultRuntime.vaults().item(owner, vaultNumber, slot));
    }
    @Override public int getContainerSize() { return size; }
    @Override public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }
    @Override public ItemStack getItem(int slot) {
        return slot >= 0 && slot < size ? items.get(slot) : ItemStack.EMPTY;
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= size || amount <= 0) return ItemStack.EMPTY;
        ItemStack removed = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= size) return ItemStack.EMPTY;
        ItemStack removed = net.minecraft.world.ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= size) return;
        ItemStack stored = stack.copy();
        if (stored.getCount() > getMaxStackSize(stored)) stored.setCount(getMaxStackSize(stored));
        items.set(slot, stored);
        setChanged();
    }
    @Override public void setChanged() {
        PersonalVaultRuntime.vaults().setVisibleItems(owner, vaultNumber, items);
    }
    @Override public boolean stillValid(Player player) {
        return player == owner && PersonalVaultRuntime.access().evaluate(owner).allowed();
    }
    @Override public void clearContent() {
        items.clear();
        setChanged();
    }
}
