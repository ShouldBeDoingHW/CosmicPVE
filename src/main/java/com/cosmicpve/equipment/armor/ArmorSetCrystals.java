package com.cosmicpve.equipment.armor;

import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.cosmicpve.data.component.ArmorSetCrystalData;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class ArmorSetCrystals {
    private ArmorSetCrystals() {}
    public static ItemStack create(ArmorSetDefinition definition, int successRate) {
        if (successRate < 1 || successRate > 100) throw new IllegalArgumentException("successRate must be in [1,100]");
        ItemStack stack = new ItemStack(ModItems.ARMOR_SET_CRYSTAL.get());
        stack.set(ModDataComponents.ARMOR_SET_CRYSTAL.get(), new ArmorSetCrystalData(
                ArmorSetCrystalData.CURRENT_DATA_VERSION, ArmorSetIdentity.from(definition), successRate));
        return stack;
    }
}
