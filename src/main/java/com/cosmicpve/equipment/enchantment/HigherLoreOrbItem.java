package com.cosmicpve.equipment.enchantment;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HigherLoreOrbItem extends Item {
    private final OrbType type;
    private final int requiredCapacity;
    private final int destinationCapacity;
    public HigherLoreOrbItem(Properties properties, OrbType type, int requiredCapacity, int destinationCapacity) {
        super(properties);
        if (destinationCapacity != requiredCapacity + 1) throw new IllegalArgumentException("Higher-lore Orb must unlock one slot");
        this.type = type; this.requiredCapacity = requiredCapacity; this.destinationCapacity = destinationCapacity;
    }
    public OrbType type() { return type; }
    public int requiredCapacity() { return requiredCapacity; }
    public int destinationCapacity() { return destinationCapacity; }
    @Override public boolean isFoil(ItemStack stack) { return false; }
    @Override public Component getName(ItemStack stack) {
        String kind = type == OrbType.ARMOR ? "Armor" : "Weapon";
        int color = type == OrbType.ARMOR ? EnchantmentOrbItem.ARMOR_NAME_COLOR : EnchantmentOrbItem.WEAPON_NAME_COLOR;
        return Component.literal(kind + " Enchantment Orb [" + destinationCapacity + "]")
                .withStyle(style -> style.withColor(color).withBold(true));
    }
    public List<Component> lore() {
        String kind = type == OrbType.ARMOR ? "Armor" : "Weapon";
        return List.of(
                Component.literal("Increases " + kind + " Enchantment Capacity to " + destinationCapacity + ".")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("Requires " + requiredCapacity + " unlocked enchantment slots.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                Component.literal("SUCCESS: 100%").withStyle(style -> style.withColor(OrbPresentationColors.SUCCESS).withBold(true)),
                Component.literal("DESTROY: 0%").withStyle(style -> style.withColor(OrbPresentationColors.DESTROY).withBold(true)));
    }
}
