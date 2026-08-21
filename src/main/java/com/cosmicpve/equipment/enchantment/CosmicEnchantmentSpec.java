package com.cosmicpve.equipment.enchantment;

import java.util.Objects;
import net.minecraft.network.chat.Component;
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

    public Component displayName() {
        return Component.translatable("enchantment." + id.getNamespace() + "." + id.getPath());
    }

    public Component description() {
        return Component.translatable("enchantment." + id.getNamespace() + "." + id.getPath() + ".description");
    }

    public Component applicability() {
        return Component.translatable("tooltip.cosmicpve.applicability." + equipmentApplicability);
    }

    public Component tierName() {
        return Component.translatable("cosmic_tier.cosmicpve." + tier.name().toLowerCase(java.util.Locale.ROOT));
    }
}
