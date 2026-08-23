package com.cosmicpve.economy;

import com.cosmicpve.data.component.BanknoteData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class Banknotes {
    private Banknotes() {}
    public static ItemStack create(long cents) {
        var stack = new ItemStack(ModItems.BANKNOTE.get());
        stack.set(ModDataComponents.BANKNOTE.get(), new BanknoteData(BanknoteData.CURRENT_DATA_VERSION, cents));
        return stack;
    }
}
