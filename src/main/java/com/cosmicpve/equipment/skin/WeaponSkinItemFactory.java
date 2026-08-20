package com.cosmicpve.equipment.skin;

import com.cosmicpve.data.component.WeaponSkinItemData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class WeaponSkinItemFactory {
    private WeaponSkinItemFactory() {}

    public static ItemStack create(Identifier skinId) {
        var definition = WeaponSkinDefinitions.find(skinId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown weapon skin: " + skinId));
        var stack = new ItemStack(ModItems.WEAPON_SKIN.get());
        stack.set(ModDataComponents.WEAPON_SKIN_ITEM.get(),
                new WeaponSkinItemData(WeaponSkinItemData.CURRENT_DATA_VERSION, skinId));
        stack.set(DataComponents.ITEM_MODEL, definition.itemModel());
        return stack;
    }
}
