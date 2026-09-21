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
        int maximumEnchantments, CosmicEnchantmentTier maximumRarity, EnchantmentLevelMode levelMode, boolean limitToAvailable) {
    public static final Codec<GeneratedEquipmentDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GeneratedEquipmentCategory.CODEC.fieldOf("category").forGetter(GeneratedEquipmentDefinitionData::category),
            Codec.INT.fieldOf("minimum_enchantments").forGetter(GeneratedEquipmentDefinitionData::minimumEnchantments),
            Codec.INT.fieldOf("maximum_enchantments").forGetter(GeneratedEquipmentDefinitionData::maximumEnchantments),
            CosmicEnchantmentTier.CODEC.fieldOf("maximum_rarity").forGetter(GeneratedEquipmentDefinitionData::maximumRarity),
            EnchantmentLevelMode.CODEC.fieldOf("level_mode").forGetter(GeneratedEquipmentDefinitionData::levelMode),
            Codec.BOOL.optionalFieldOf("limit_to_available", false).forGetter(GeneratedEquipmentDefinitionData::limitToAvailable)
    ).apply(instance, GeneratedEquipmentDefinitionData::new));

    public GeneratedEquipmentDefinitionData(GeneratedEquipmentCategory category,int minimumEnchantments,int maximumEnchantments,CosmicEnchantmentTier maximumRarity,EnchantmentLevelMode levelMode) { this(category,minimumEnchantments,maximumEnchantments,maximumRarity,levelMode,false); }

    public ValidationResult<GeneratedEquipmentDefinition> resolve(String source, Registry<Enchantment> enchantments) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        if (minimumEnchantments < 1 || maximumEnchantments < minimumEnchantments)
            diagnostics.add(ContentDiagnostic.error(source, "generated enchantment range must satisfy 1 <= minimum <= maximum"));
        var capacity = new CustomEnchantCapacityService();
        for (var item : List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
            var stack = new ItemStack(item);
            long candidates = CosmicEnchantmentSpecs.ALL.stream().filter(spec -> spec.randomPoolEligible())
                    .filter(spec -> spec.tier().ordinal() <= maximumRarity.ordinal())
                    .filter(spec -> enchantments.get(spec.id()).isPresent())
                    .filter(spec -> appliesToIronArmorSlot(spec.equipmentApplicability(), item))
                    .count();
            if ((limitToAvailable ? minimumEnchantments : maximumEnchantments) > candidates || maximumEnchantments > capacity.capacity(stack)) {
                diagnostics.add(ContentDiagnostic.error(source, "generated equipment cannot fulfill maximum enchantment count for "
                        + item.builtInRegistryHolder().unwrapKey().orElseThrow().identifier()));
            }
        }
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new GeneratedEquipmentDefinition(
                category, minimumEnchantments, maximumEnchantments, maximumRarity, levelMode, limitToAvailable));
    }

    private static boolean appliesToIronArmorSlot(String applicability, net.minecraft.world.item.Item item) {
        return switch (applicability) {
            case "any_armor" -> true;
            case "helmet" -> item == Items.IRON_HELMET;
            case "chestplate" -> item == Items.IRON_CHESTPLATE;
            case "leggings" -> item == Items.IRON_LEGGINGS;
            case "boots_or_leggings" -> item == Items.IRON_BOOTS || item == Items.IRON_LEGGINGS;
            default -> false;
        };
    }
}
