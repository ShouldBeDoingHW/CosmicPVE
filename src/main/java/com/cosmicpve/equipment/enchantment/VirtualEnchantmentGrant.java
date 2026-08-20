package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/** Runtime-only grant; it is never written to an ItemStack. */
public record VirtualEnchantmentGrant(Identifier enchantmentId, int level, Identifier sourceId) {
    public VirtualEnchantmentGrant {
        enchantmentId = Objects.requireNonNull(enchantmentId);
        sourceId = Objects.requireNonNull(sourceId);
        if (level <= 0 || level > 255) {
            throw new IllegalArgumentException("Virtual enchantment level must be between 1 and 255");
        }
    }
}
