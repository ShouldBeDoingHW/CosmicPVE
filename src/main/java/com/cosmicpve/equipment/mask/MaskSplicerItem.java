package com.cosmicpve.equipment.mask;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
public final class MaskSplicerItem extends Item {
    public MaskSplicerItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) { return super.getName(stack).copy().withColor(0x450000); }
}
