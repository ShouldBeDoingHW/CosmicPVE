package com.cosmicpve.equipment.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** White Scroll presentation; protection remains stored on the target equipment. */
public final class WhiteScrollItem extends Item {
    public WhiteScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withBold(true));
    }
}
