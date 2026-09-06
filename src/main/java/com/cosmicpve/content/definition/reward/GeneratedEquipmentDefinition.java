package com.cosmicpve.content.definition.reward;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;

public record GeneratedEquipmentDefinition(GeneratedEquipmentCategory category, int minimumEnchantments,
        int maximumEnchantments, CosmicEnchantmentTier maximumRarity, EnchantmentLevelMode levelMode, boolean limitToAvailable) {
    public GeneratedEquipmentDefinition(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,false); }
}
