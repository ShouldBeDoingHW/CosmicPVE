package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CosmicDustItem extends Item {
    public CosmicDustItem(Properties properties) { super(properties); }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.COSMIC_DUST.get());
        if (data == null) return super.getName(stack).copy().withStyle(style -> style.withBold(true));
        return Component.translatable("item.cosmicpve.cosmic_dust.named",
                Component.translatable("rarity.cosmicpve." + data.tier().serializedName()))
                .withStyle(style -> style.withColor(data.tier().tooltipColor()).withBold(true));
    }
}
