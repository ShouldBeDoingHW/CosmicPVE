package com.cosmicpve.equipment.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public final class EnchantmentOrbItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final boolean FORCE_GLINT = false;
    public static final int ARMOR_NAME_COLOR = 0x55FFFF;
    public static final int WEAPON_NAME_COLOR = 0xFFAA00;

    public EnchantmentOrbItem(Properties properties) { super(properties); }

    @Override
    public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }

    @Override
    public Component getName(ItemStack stack) {
        int color = stack.is(com.cosmicpve.registry.ModItems.ARMOR_ENCHANTMENT_ORB.get())
                ? ARMOR_NAME_COLOR : WEAPON_NAME_COLOR;
        String name = stack.is(com.cosmicpve.registry.ModItems.ARMOR_ENCHANTMENT_ORB.get())
                ? "Armor Enchantment Orb" : "Weapon Enchantment Orb";
        return Component.literal(name).withStyle(style -> style.withColor(color).withBold(true));
    }

    public static java.util.List<Component> lore(ItemStack stack, com.cosmicpve.data.component.EnchantmentOrbData data) {
        boolean armor = stack.is(com.cosmicpve.registry.ModItems.ARMOR_ENCHANTMENT_ORB.get());
        return java.util.List.of(
                Component.literal(armor ? "Expand the weave. Make room for one more enchantment."
                                : "Carve another channel for power.")
                        .withStyle(style -> style.withColor(0xFFFF55).withItalic(true)),
                Component.empty(),
                Component.literal("SUCCESS: " + data.successRate() + "%")
                        .withStyle(style -> style.withColor(0x55FF55).withBold(true)),
                Component.literal("DESTROY: " + data.destroyRate() + "%")
                        .withStyle(style -> style.withColor(0xFF5555).withBold(true)),
                Component.literal("On success: +1 Cosmic Enchantment Slot").withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal(armor ? "Armor capacity: 5 → 8 max" : "Weapon capacity: 5 → 10 max")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("White Scroll can protect a destructive failure.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
