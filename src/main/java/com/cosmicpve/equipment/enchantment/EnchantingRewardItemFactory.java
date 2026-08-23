package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.BlackScrollData;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Exact reward construction seam for later reward tables; contains no weighting policy. */
public final class EnchantingRewardItemFactory {
    public ItemStack blackScroll(int returnedSuccessRate) {
        var stack = new ItemStack(ModItems.BLACK_SCROLL.get());
        stack.set(ModDataComponents.BLACK_SCROLL.get(), new BlackScrollData(
                BlackScrollData.CURRENT_DATA_VERSION, returnedSuccessRate));
        return stack;
    }

    public ItemStack orb(OrbType type, int successRate, RandomSource random) {
        return orb(type, successRate, random.nextInt(100) + 1);
    }

    public ItemStack orb(OrbType type, int successRate, int destroyRate) {
        var stack = new ItemStack(type == OrbType.ARMOR
                ? ModItems.ARMOR_ENCHANTMENT_ORB.get() : ModItems.WEAPON_ENCHANTMENT_ORB.get());
        stack.set(ModDataComponents.ENCHANTMENT_ORB.get(), new EnchantmentOrbData(
                EnchantmentOrbData.CURRENT_DATA_VERSION, successRate, destroyRate));
        return stack;
    }
}
