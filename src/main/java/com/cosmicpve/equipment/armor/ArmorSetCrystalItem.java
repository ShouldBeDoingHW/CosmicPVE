package com.cosmicpve.equipment.armor;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ArmorSetCrystalItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final boolean FORCE_GLINT = true;
    public ArmorSetCrystalItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
        if (data == null) return super.getName(stack).copy().withStyle(style -> style.withBold(true));
        return Component.translatable("item.cosmicpve.armor_set_crystal.named", data.identity().displayName())
                .withStyle(style -> style.withColor(data.identity().color()).withBold(true));
    }

    @Override public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }
}
