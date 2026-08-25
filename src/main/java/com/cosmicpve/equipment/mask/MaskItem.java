package com.cosmicpve.equipment.mask;

import com.cosmicpve.data.component.MaskPresentation;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MaskItem extends Item {
    public MaskItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        var loadout = stack.get(ModDataComponents.MASK_ITEM.get());
        if (loadout == null || !loadout.valid()) return super.getName(stack);
        var presentations = loadout.presentations();
        if (presentations.size() != loadout.maskIds().size()) return super.getName(stack);
        if (presentations.size() == 1) return Component.translatable("item.cosmicpve.mask.named",
                presentations.getFirst().displayName()).withColor(presentations.getFirst().color());
        net.minecraft.network.chat.MutableComponent names = Component.empty();
        for (int i = 0; i < presentations.size(); i++) {
            if (i > 0) names.append(Component.literal(", "));
            MaskPresentation presentation = presentations.get(i);
            names.append(presentation.displayName().copy().withColor(presentation.color()));
        }
        return Component.translatable("item.cosmicpve.mask.multi", names).withColor(0xAA00AA);
    }
}
