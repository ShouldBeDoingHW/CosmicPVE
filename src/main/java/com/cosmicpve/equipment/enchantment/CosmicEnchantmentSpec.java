package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record CosmicEnchantmentSpec(
        Identifier id, int maxLevel, CosmicEnchantmentTier tier, String equipmentApplicability) {
    public CosmicEnchantmentSpec {
        id = Objects.requireNonNull(id);
        if (maxLevel <= 0) {
            throw new IllegalArgumentException("Enchantment max level must be positive");
        }
        tier = Objects.requireNonNull(tier);
        equipmentApplicability = Objects.requireNonNull(equipmentApplicability);
    }
}
