package com.cosmicpve.equipment.accessory;

import com.cosmicpve.data.component.AccessoryItemData;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class AmuletItemFactory {
    private AmuletItemFactory() {}
    public static ItemStack create(AmuletDefinition definition) {
        var item = switch (definition) {
            case BLOOD_DIAMOND -> ModItems.BLOOD_DIAMOND_AMULET.get();
            case ICICLE -> ModItems.ICICLE_AMULET.get();
            case BLACK_HEART -> ModItems.BLACK_HEART_AMULET.get();
            case LUAU_LEI -> ModItems.LUAU_LEI_AMULET.get();
        };
        var stack = new ItemStack(item);
        stack.set(ModDataComponents.ACCESSORY_ITEM.get(),
                new AccessoryItemData(AccessoryItemData.DATA_VERSION, AccessorySlot.AMULET, definition.id()));
        return stack;
    }
}
