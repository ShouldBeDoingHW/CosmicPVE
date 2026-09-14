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
        return Component.literal("White Scroll").withStyle(style -> style.withColor(0xFFFFFF).withBold(true));
    }

    public static java.util.List<Component> lore() {
        return java.util.List.of(
                Component.literal("Prevents an item from being destroyed due to a failed enchantment book. Place scroll on item to apply!")
                        .withStyle(style -> style.withColor(0x55FFFF).withItalic(true)));
    }
}
