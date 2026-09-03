package com.cosmicpve.equipment.enchantment;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HolyWhiteScrollItem extends Item {
    public HolyWhiteScrollItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) {
        MutableComponent name = Component.literal("* ")
                .withStyle(style -> style.withColor(0xC4394A).withBold(true));
        name.append(Component.literal("Holy").withStyle(style ->
                style.withColor(0xC4394A).withUnderlined(true).withBold(true)));
        name.append(Component.literal(" * ").withStyle(style -> style.withColor(0xC4394A).withBold(true)));
        name.append(Component.literal("Whitescroll").withStyle(style -> style.withColor(0xFFFFFF).withBold(true)));
        return name;
    }
    public static List<Component> lore() {
        return List.of(
                Component.literal("Apply to an item with an already applied whitescroll")
                        .withStyle(style -> style.withColor(0xC4394A).withItalic(true)),
                Component.literal("to consume it and turn the item holy!")
                        .withStyle(style -> style.withColor(0xC4394A).withItalic(true)),
                Component.literal("Holy items have a 50% chance to be preserved upon")
                        .withStyle(style -> style.withColor(0xC4394A).withItalic(true).withUnderlined(true)),
                Component.literal("death in a non keep-inventory death!")
                        .withStyle(style -> style.withColor(0xC4394A).withItalic(true)));
    }
}
