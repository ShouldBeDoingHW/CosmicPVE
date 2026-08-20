package com.cosmicpve.equipment.enchantment;

import net.minecraft.world.item.ItemStack;

/** Keeps generated output safe even when every player inventory slot is occupied. */
public final class BlackScrollCursorOutput {
    private BlackScrollCursorOutput() {}

    public static ItemStack afterApplication(ItemStack originalCursor, BlackScrollExtractionResult result) {
        return result.succeeded() ? result.returnedBook() : originalCursor;
    }
}
