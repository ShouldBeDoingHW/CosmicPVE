package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public final class CustomEnchantCapacityService {
    public static final int BASE_CAPACITY = 5;
    public static final int ARMOR_MAX_ORB_BONUS = 3;
    public static final int WEAPON_MAX_ORB_BONUS = 5;

    public int capacity(ItemStack stack) {
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        if (metadata == null) return BASE_CAPACITY;
        return metadata.slotLimit() + Math.min(metadata.orbUpgrades(), maxOrbBonus(stack));
    }

    public int orbUpgrades(ItemStack stack) {
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        return metadata == null ? 0 : Math.min(metadata.orbUpgrades(), maxOrbBonus(stack));
    }

    public int maxOrbBonus(ItemStack stack) {
        if (isArmor(stack)) return ARMOR_MAX_ORB_BONUS;
        if (isWeapon(stack)) return WEAPON_MAX_ORB_BONUS;
        return 0;
    }

    public boolean canUpgrade(ItemStack stack, OrbType type) {
        return type.matches(stack) && orbUpgrades(stack) < type.maximumBonus();
    }

    public boolean incrementOrbUpgrade(ItemStack stack, OrbType type) {
        if (!canUpgrade(stack, type)) return false;
        var metadata = stack.getOrDefault(ModDataComponents.CUSTOM_ENCHANT_META.get(), CustomEnchantMetadata.DEFAULT);
        stack.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), metadata.withOrbUpgrades(metadata.orbUpgrades() + 1));
        return true;
    }

    public static boolean isArmor(ItemStack stack) {
        return com.cosmicpve.equipment.armor.ArmorSetResolver.isArmor(stack);
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)
                || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }
    public int used(ItemStack stack) {
        return (int) EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream()
                .filter(entry -> entry.getKey().unwrapKey().map(key -> CosmicEnchantmentSpecs.find(key.identifier()).isPresent())
                        .orElse(false)).count();
    }
    public boolean canAdd(ItemStack stack, net.minecraft.resources.Identifier id) {
        boolean exists = EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream()
                .anyMatch(entry -> entry.getKey().unwrapKey().map(key -> key.identifier().equals(id)).orElse(false));
        return canAdd(used(stack), capacity(stack), exists);
    }
    public static boolean canAdd(int used, int capacity, boolean existingEnchant) {
        return existingEnchant || used < capacity;
    }
}
