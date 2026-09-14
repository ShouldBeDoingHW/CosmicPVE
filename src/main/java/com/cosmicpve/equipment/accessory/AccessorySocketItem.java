package com.cosmicpve.equipment.accessory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Canonical presentation for concrete accessory sockets. */
public final class AccessorySocketItem extends Item {
    public static final int COLOR = 0x055251;

    public AccessorySocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(COLOR).withBold(true));
    }
}
