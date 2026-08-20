package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CosmicEnchantmentBookItem extends Item {
    public static final boolean FORCE_GLINT = true;
    public CosmicEnchantmentBookItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }
    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        if (data == null) return super.getName(stack);
        var spec = CosmicEnchantmentSpecs.find(data.enchantmentId());
        return Component.translatable("enchantment." + data.enchantmentId().getNamespace() + "."
                + data.enchantmentId().getPath()).append(" ").append(Component.translatable("enchantment.level." + data.level()))
                .withColor(spec.map(value -> value.tier().tooltipColor()).orElse(0xFFFFFF));
    }
}
