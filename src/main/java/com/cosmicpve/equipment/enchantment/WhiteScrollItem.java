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
                Component.literal("A thin veil between fortune and ruin.")
                        .withStyle(style -> style.withColor(0xFFFF55).withItalic(true)),
                Component.empty(),
                Component.literal("ONE-TIME PROTECTION")
                        .withStyle(style -> style.withColor(0x55FFFF).withBold(true)),
                Component.literal("Apply to eligible equipment.").withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Protects against one destructive failed application.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Consumed only when it actually prevents destruction.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
