package com.cosmicpve.equipment.heroic;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Centralized Heroic Crystal name and concise player-facing lore presentation. */
public final class HeroicCrystalItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final int NAME_COLOR = 0xAA00AA;

    public HeroicCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withColor(NAME_COLOR);
    }

    public static List<Component> lore() {
        return List.of(
                Component.translatable("tooltip.cosmicpve.heroic_crystal.purpose")
                        .withStyle(ChatFormatting.YELLOW),
                Component.translatable("tooltip.cosmicpve.heroic_crystal.instruction")
                        .withStyle(ChatFormatting.YELLOW));
    }
}
