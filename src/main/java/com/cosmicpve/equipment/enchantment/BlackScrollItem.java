package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BlackScrollItem extends Item {
    public static final int MAX_STACK_SIZE = 1;

    public BlackScrollItem(Properties properties) { super(properties); }

    @Override
    public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.BLACK_SCROLL.get());
        Component name = data == null ? super.getName(stack)
                : Component.translatable("item.cosmicpve.black_scroll.rated", data.returnedSuccessRate());
        return name.copy().withStyle(style -> style.withBold(true));
    }
}
