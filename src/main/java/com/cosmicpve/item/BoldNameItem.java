package com.cosmicpve.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Simple item presentation for utility items whose localized name is always bold. */
public final class BoldNameItem extends Item {
    public BoldNameItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withBold(true));
    }
}
