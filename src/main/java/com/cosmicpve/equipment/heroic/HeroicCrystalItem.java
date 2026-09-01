package com.cosmicpve.equipment.heroic;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Centralized Heroic Crystal name and concise player-facing lore presentation. */
public final class HeroicCrystalItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final int NAME_COLOR = 0xFF00A2;
    public static final boolean FORCE_GLINT = true;

    public HeroicCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Heroic Crystal")
                .withStyle(style -> style.withColor(NAME_COLOR).withBold(true));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return FORCE_GLINT;
    }

    public static List<Component> lore() {
        return List.of(
                Component.literal("A shard that refuses to let ordinary gear stay ordinary.")
                        .withStyle(style -> style.withColor(0xFFFF55).withItalic(true)),
                Component.empty(),
                Component.literal("ONE-TIME HEROIC UPGRADE")
                        .withStyle(style -> style.withColor(NAME_COLOR).withBold(true)),
                Component.literal("Armor / Pickaxes / Shovels: +250 Maximum Durability")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Existing current durability also increases by 250")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Dungeon Portals: Converts to Heroic")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Armor assumes Heroic leather form; tools assume Heroic gold form.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
