package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.equipment.armor.ArmorSetResolver;
import net.minecraft.world.item.ItemStack;

public final class EquipmentInteractionPolicy {
    private EquipmentInteractionPolicy() {}
    public static boolean isPotentialEquipment(ItemStack stack) {
        return !stack.isEmpty() && (stack.isDamageableItem() || ArmorSetResolver.isArmor(stack));
    }
}
