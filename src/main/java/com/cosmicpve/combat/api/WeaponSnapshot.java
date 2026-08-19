package com.cosmicpve.combat.api;

import net.minecraft.world.item.ItemStack;

/** A defensive ItemStack copy captured when an attack context is created. */
public final class WeaponSnapshot {
    private final ItemStack stack;

    public WeaponSnapshot(ItemStack stack) {
        this.stack = stack.copy();
    }

    public static WeaponSnapshot empty() {
        return new WeaponSnapshot(ItemStack.EMPTY);
    }

    public ItemStack stack() {
        return stack.copy();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
