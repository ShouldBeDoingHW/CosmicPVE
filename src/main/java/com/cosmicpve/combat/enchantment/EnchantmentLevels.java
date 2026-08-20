package com.cosmicpve.combat.enchantment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchantmentLevels {
    private EnchantmentLevels() {}

    static int onStack(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (entry.getKey().unwrapKey().filter(enchantment::equals).isPresent()) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
