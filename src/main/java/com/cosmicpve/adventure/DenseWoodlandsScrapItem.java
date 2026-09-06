package com.cosmicpve.adventure;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DenseWoodlandsScrapItem extends Item {
    public DenseWoodlandsScrapItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(s -> s.withColor(0x43B03C).withBold(true));
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.accept(Component.literal("A fragment recovered from the Dense Woodlands.")
                .withStyle(ChatFormatting.YELLOW));
    }
}
