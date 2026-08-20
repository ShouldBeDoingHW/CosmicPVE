package com.cosmicpve.equipment.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EnchantmentOrbItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final boolean FORCE_GLINT = false;

    public EnchantmentOrbItem(Properties properties) { super(properties); }

    @Override
    public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }
}
