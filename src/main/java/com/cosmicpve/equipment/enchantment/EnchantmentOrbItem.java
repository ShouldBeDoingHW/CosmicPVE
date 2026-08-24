package com.cosmicpve.equipment.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public final class EnchantmentOrbItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final boolean FORCE_GLINT = false;
    public static final int NAME_COLOR = 0x55FF55;

    public EnchantmentOrbItem(Properties properties) { super(properties); }

    @Override
    public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }

    @Override
    public Component getName(ItemStack stack) { return super.getName(stack).copy().withColor(NAME_COLOR); }
}
