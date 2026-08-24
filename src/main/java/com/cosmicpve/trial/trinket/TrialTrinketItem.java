package com.cosmicpve.trial.trinket;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public final class TrialTrinketItem extends Item {
    public TrialTrinketItem(Properties properties) { super(properties); }

    @Override
    public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.TRIAL_TRINKET.get());
        return data == null || !data.valid() ? super.getName(stack)
                : super.getName(stack).copy().withColor(data.type().presentationColor());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.cosmicpve.trial_trinket.purpose")
                .withStyle(net.minecraft.ChatFormatting.YELLOW));
        tooltip.accept(Component.translatable("tooltip.cosmicpve.trial_trinket.instruction")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
