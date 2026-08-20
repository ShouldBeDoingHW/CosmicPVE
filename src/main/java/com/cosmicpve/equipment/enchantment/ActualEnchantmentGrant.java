package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record ActualEnchantmentGrant(Identifier enchantmentId, int level, Identifier sourceId) {
    public ActualEnchantmentGrant {
        enchantmentId = Objects.requireNonNull(enchantmentId);
        sourceId = Objects.requireNonNull(sourceId);
        if (level <= 0 || level > 255) {
            throw new IllegalArgumentException("Actual enchantment level must be between 1 and 255");
        }
    }
}
