package com.cosmicpve.equipment.accessory;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BeltItem extends Item {
    public BeltItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.ACCESSORY_ITEM.get());
        if (data == null || !data.valid()) return super.getName(stack);
        return BeltDefinition.find(data.accessoryId()).<Component>map(value -> Component.literal(value.displayName())
                .withStyle(style -> style.withColor(value.color()).withBold(true))).orElseGet(() -> super.getName(stack));
    }
}
