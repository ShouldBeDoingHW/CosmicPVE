package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record EnchantmentProvenance(EnchantmentSourceKind kind, Identifier sourceId, int level) {
    public EnchantmentProvenance {
        kind = Objects.requireNonNull(kind);
        sourceId = Objects.requireNonNull(sourceId);
        if (level <= 0 || level > 255) {
            throw new IllegalArgumentException("Enchantment source level must be between 1 and 255");
        }
    }
}
