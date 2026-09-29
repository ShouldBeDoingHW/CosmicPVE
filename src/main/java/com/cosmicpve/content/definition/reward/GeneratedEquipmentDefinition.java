package com.cosmicpve.content.definition.reward;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;

public record GeneratedEquipmentDefinition(GeneratedEquipmentCategory category, int minimumEnchantments,
        int maximumEnchantments, CosmicEnchantmentTier maximumRarity, EnchantmentLevelMode levelMode,
        boolean limitToAvailable, VanillaArmorEnchantProfile vanillaArmorEnchantProfile,
        VanillaWeaponEnchantProfile vanillaWeaponEnchantProfile) {
    public GeneratedEquipmentDefinition(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,false,VanillaArmorEnchantProfile.NONE,VanillaWeaponEnchantProfile.NONE); }
    public GeneratedEquipmentDefinition(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode,boolean limitToAvailable) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,limitToAvailable,VanillaArmorEnchantProfile.NONE,VanillaWeaponEnchantProfile.NONE); }
    public GeneratedEquipmentDefinition(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode,boolean limitToAvailable,VanillaArmorEnchantProfile armorProfile) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,limitToAvailable,armorProfile,VanillaWeaponEnchantProfile.NONE); }
}
