package com.cosmicpve.content.definition.reward;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;

public record GeneratedEquipmentDefinition(GeneratedEquipmentCategory category, int minimumEnchantments,
        int maximumEnchantments, CosmicEnchantmentTier maximumRarity, EnchantmentLevelMode levelMode) {}
