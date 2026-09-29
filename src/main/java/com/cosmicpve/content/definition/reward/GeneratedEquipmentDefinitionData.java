package com.cosmicpve.content.definition.reward;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public record GeneratedEquipmentDefinitionData(GeneratedEquipmentCategory category, int minimumEnchantments,
        int maximumEnchantments, CosmicEnchantmentTier maximumRarity, EnchantmentLevelMode levelMode,
        boolean limitToAvailable, VanillaArmorEnchantProfile vanillaArmorEnchantProfile,
        VanillaWeaponEnchantProfile vanillaWeaponEnchantProfile) {
    public static final Codec<GeneratedEquipmentDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GeneratedEquipmentCategory.CODEC.fieldOf("category").forGetter(GeneratedEquipmentDefinitionData::category),
            Codec.INT.fieldOf("minimum_enchantments").forGetter(GeneratedEquipmentDefinitionData::minimumEnchantments),
            Codec.INT.fieldOf("maximum_enchantments").forGetter(GeneratedEquipmentDefinitionData::maximumEnchantments),
            CosmicEnchantmentTier.CODEC.fieldOf("maximum_rarity").forGetter(GeneratedEquipmentDefinitionData::maximumRarity),
            EnchantmentLevelMode.CODEC.fieldOf("level_mode").forGetter(GeneratedEquipmentDefinitionData::levelMode),
            Codec.BOOL.optionalFieldOf("limit_to_available", false).forGetter(GeneratedEquipmentDefinitionData::limitToAvailable),
            VanillaArmorEnchantProfile.CODEC.optionalFieldOf("vanilla_armor_enchant_profile", VanillaArmorEnchantProfile.NONE)
                    .forGetter(GeneratedEquipmentDefinitionData::vanillaArmorEnchantProfile),
            VanillaWeaponEnchantProfile.CODEC.optionalFieldOf("vanilla_weapon_enchant_profile", VanillaWeaponEnchantProfile.NONE)
                    .forGetter(GeneratedEquipmentDefinitionData::vanillaWeaponEnchantProfile)
    ).apply(instance, GeneratedEquipmentDefinitionData::new));

    public GeneratedEquipmentDefinitionData(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,false,VanillaArmorEnchantProfile.NONE,VanillaWeaponEnchantProfile.NONE); }
    public GeneratedEquipmentDefinitionData(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode,boolean limitToAvailable,VanillaArmorEnchantProfile armorProfile) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,limitToAvailable,armorProfile,VanillaWeaponEnchantProfile.NONE); }

    public ValidationResult<GeneratedEquipmentDefinition> resolve(String source, Registry<Enchantment> enchantments) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        if (minimumEnchantments < 1 || maximumEnchantments < minimumEnchantments)
            diagnostics.add(ContentDiagnostic.error(source, "generated enchantment range must satisfy 1 <= minimum <= maximum"));
        var capacity = new CustomEnchantCapacityService();
        var items = category == GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE
                ? List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)
                : List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.BOW, Items.CROSSBOW);
        if (category == GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE
                && vanillaWeaponEnchantProfile != VanillaWeaponEnchantProfile.NONE)
            diagnostics.add(ContentDiagnostic.error(source, "armor row cannot use weapon enchant profile"));
        if (category == GeneratedEquipmentCategory.RANDOM_WEAPON
                && vanillaArmorEnchantProfile != VanillaArmorEnchantProfile.NONE)
            diagnostics.add(ContentDiagnostic.error(source, "weapon row cannot use armor enchant profile"));
        for (var item : items) {
            var stack = new ItemStack(item);
            long candidates = CosmicEnchantmentSpecs.ALL.stream().filter(spec -> spec.randomPoolEligible())
                    .filter(spec -> spec.tier().ordinal() <= maximumRarity.ordinal())
                    .filter(spec -> enchantments.get(spec.id()).isPresent())
                    .filter(spec -> appliesToItem(spec.equipmentApplicability(), item))
                    .count();
            if ((!limitToAvailable && maximumEnchantments > candidates)
                    || (limitToAvailable && candidates == 0) || maximumEnchantments > capacity.capacity(stack)) {
                diagnostics.add(ContentDiagnostic.error(source, "generated equipment cannot fulfill maximum enchantment count for "
                        + item.builtInRegistryHolder().unwrapKey().orElseThrow().identifier()));
            }
        }
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new GeneratedEquipmentDefinition(
                category, minimumEnchantments, maximumEnchantments, maximumRarity, levelMode, limitToAvailable,
                vanillaArmorEnchantProfile, vanillaWeaponEnchantProfile));
    }

    private static boolean appliesToItem(String applicability, net.minecraft.world.item.Item item) {
        return switch (applicability) {
            case "any_armor" -> true;
            case "helmet" -> item == Items.IRON_HELMET;
            case "chestplate" -> item == Items.IRON_CHESTPLATE;
            case "leggings" -> item == Items.IRON_LEGGINGS;
            case "boots_or_leggings" -> item == Items.IRON_BOOTS || item == Items.IRON_LEGGINGS;
            case "boots" -> item == Items.IRON_BOOTS;
            case "boots_or_chestplate" -> item == Items.IRON_BOOTS || item == Items.IRON_CHESTPLATE;
            case "chestplate_or_leggings" -> item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS;
            case "helmet_or_chestplate" -> item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE;
            case "sword" -> item == Items.DIAMOND_SWORD;
            case "axe" -> item == Items.DIAMOND_AXE;
            case "bow" -> item == Items.BOW;
            case "crossbow" -> item == Items.CROSSBOW;
            case "sword_or_axe" -> item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE;
            case "bow_or_crossbow" -> item == Items.BOW || item == Items.CROSSBOW;
            case "all_weapons" -> item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE
                    || item == Items.BOW || item == Items.CROSSBOW;
            default -> false;
        };
    }
}
