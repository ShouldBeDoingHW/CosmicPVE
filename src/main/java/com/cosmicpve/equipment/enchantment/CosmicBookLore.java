package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Standard lore assembled entirely from generic book data and enchantment specification metadata. */
public final class CosmicBookLore {
    private CosmicBookLore() {}

    public static List<Component> lines(CosmicEnchantmentBookData book, CosmicEnchantmentSpec spec) {
        int tierColor = spec.tier().tooltipColor();
        return List.of(
                Component.translatable("tooltip.cosmicpve.book.success", book.successRate())
                        .withColor(ItemApplicationColors.SUCCESS),
                Component.translatable("tooltip.cosmicpve.book.destroy", book.destroyRate())
                        .withColor(ItemApplicationColors.DESTROY),
                Component.translatable("tooltip.cosmicpve.book.description",
                                spec.tierName().copy().withColor(tierColor), spec.description())
                        .withStyle(ChatFormatting.YELLOW),
                spec.applicability().copy().withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.cosmicpve.book.instruction").withStyle(ChatFormatting.GRAY));
    }
}
