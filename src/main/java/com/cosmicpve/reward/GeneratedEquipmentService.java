package com.cosmicpve.reward;

import com.cosmicpve.content.definition.reward.EnchantmentLevelMode;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentDefinition;
import com.cosmicpve.content.definition.reward.VanillaArmorEnchantProfile;
import com.cosmicpve.content.definition.reward.VanillaWeaponEnchantProfile;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentCategory;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class GeneratedEquipmentService {
    private static final List<Item> IRON_ARMOR = List.of(
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
    private static final List<Item> WEAPONS = List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.BOW, Items.CROSSBOW);
    private final CustomEnchantCapacityService capacity = new CustomEnchantCapacityService();

    public ItemStack generate(GeneratedEquipmentDefinition definition, Registry<Enchantment> enchantments,
            RandomSource random) {
        ItemStack result = new ItemStack(selectItem(definition.category(), random));
        List<CosmicEnchantmentSpec> candidates = candidates(result, definition, enchantments);
        shuffle(candidates, random);
        int requested = random.nextIntBetweenInclusive(definition.minimumEnchantments(), definition.maximumEnchantments());
        if (definition.limitToAvailable()) requested = Math.min(requested, candidates.size());
        if (requested > candidates.size() || requested > capacity.capacity(result)) {
            throw new IllegalStateException("Validated generated-equipment definition cannot produce " + requested
                    + " distinct enchantments for " + result.getItem());
        }
        int count = requested;
        EnchantmentHelper.updateEnchantments(result, mutable -> {
            for (int index = 0; index < count; index++) {
                CosmicEnchantmentSpec spec = candidates.get(index);
                int level = definition.levelMode() == EnchantmentLevelMode.MAXIMUM
                        ? spec.maxLevel() : random.nextIntBetweenInclusive(1, spec.maxLevel());
                mutable.set(enchantments.get(spec.id()).orElseThrow(), level);
            }
            if (definition.vanillaArmorEnchantProfile() != VanillaArmorEnchantProfile.NONE) {
                mutable.set(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
                mutable.set(enchantments.getOrThrow(Enchantments.PROTECTION), 4);
                if (definition.vanillaArmorEnchantProfile() == VanillaArmorEnchantProfile.PROTECTION_UNBREAKING_MENDING)
                    mutable.set(enchantments.getOrThrow(Enchantments.MENDING), 1);
            }
            if (definition.vanillaWeaponEnchantProfile() != VanillaWeaponEnchantProfile.NONE) {
                Item item = result.getItem();
                mutable.set(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
                if (item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE) {
                    mutable.set(enchantments.getOrThrow(Enchantments.SHARPNESS), 5);
                    if (item == Items.DIAMOND_SWORD) mutable.set(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 2);
                } else if (item == Items.BOW) {
                    mutable.set(enchantments.getOrThrow(Enchantments.POWER), 5);
                    mutable.set(enchantments.getOrThrow(Enchantments.INFINITY), 1);
                    mutable.set(enchantments.getOrThrow(Enchantments.FLAME), 1);
                } else if (item == Items.CROSSBOW) {
                    mutable.set(enchantments.getOrThrow(Enchantments.QUICK_CHARGE), 3);
                }
                if (definition.vanillaWeaponEnchantProfile() == VanillaWeaponEnchantProfile.STANDARD_MASTERY
                        && item != Items.BOW) mutable.set(enchantments.getOrThrow(Enchantments.MENDING), 1);
            }
        });
        result.set(DataComponents.CUSTOM_NAME, Component.literal(GeneratedEquipmentNames.roll(result.getItem(), random))
                .withStyle(style -> style.withItalic(false)));
        return result;
    }

    public static Item selectItem(GeneratedEquipmentCategory category, RandomSource random) {
        if (category == GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE)
            return IRON_ARMOR.get(random.nextInt(IRON_ARMOR.size()));
        int roll = random.nextInt(10);
        return WEAPONS.get(roll < 3 ? 0 : roll < 6 ? 1 : roll < 8 ? 2 : 3);
    }

    public List<CosmicEnchantmentSpec> candidates(ItemStack stack, GeneratedEquipmentDefinition definition,
            Registry<Enchantment> enchantments) {
        return new ArrayList<>(CosmicEnchantmentSpecs.ALL.stream()
                .filter(CosmicEnchantmentSpec::randomPoolEligible)
                .filter(spec -> spec.tier().ordinal() <= definition.maximumRarity().ordinal())
                .filter(spec -> enchantments.get(spec.id())
                        .filter(holder -> holder.value().canEnchant(stack)).isPresent())
                .toList());
    }

    private static <T> void shuffle(List<T> values, RandomSource random) {
        for (int index = values.size() - 1; index > 0; index--) {
            int replacement = random.nextInt(index + 1);
            T value = values.get(index);
            values.set(index, values.get(replacement));
            values.set(replacement, value);
        }
    }
}
