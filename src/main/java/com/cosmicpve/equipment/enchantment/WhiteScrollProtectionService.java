package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class WhiteScrollProtectionService {
    public boolean isProtected(ItemStack stack) {
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        return metadata != null && metadata.whiteScrollProtected();
    }
    public boolean apply(ItemStack stack) {
        if (isProtected(stack)) return false;
        set(stack, true);
        return true;
    }
    public boolean consumeIfProtected(ItemStack stack) {
        if (!isProtected(stack)) return false;
        set(stack, false);
        return true;
    }
    private static void set(ItemStack stack, boolean protectedState) {
        var current = stack.getOrDefault(ModDataComponents.CUSTOM_ENCHANT_META.get(), CustomEnchantMetadata.DEFAULT);
        stack.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), current.withWhiteScrollProtected(protectedState));
    }
}
