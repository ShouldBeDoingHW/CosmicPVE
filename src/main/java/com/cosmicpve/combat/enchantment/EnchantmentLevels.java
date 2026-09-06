package com.cosmicpve.combat.enchantment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantmentLevels {
    private EnchantmentLevels() {}

    public static int onStack(net.minecraft.world.entity.LivingEntity owner, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (com.cosmicpve.adventure.AdventureRules.restricted(owner) && !com.cosmicpve.adventure.AdventureRules.allows(enchantment.identifier())) return 0;
        return onStack(stack,enchantment);
    }

    public static int onStack(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (entry.getKey().unwrapKey().filter(enchantment::equals).isPresent()) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
