package com.cosmicpve.equipment.enchantment;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public record BookOpeningRateContext(CosmicEnchantmentTier tier, @Nullable ServerPlayer opener) {
    public BookOpeningRateContext {
        if (tier == null) throw new IllegalArgumentException("Book-opening tier is required");
    }
}
