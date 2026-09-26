package com.cosmicpve.equipment.accessory;

import com.cosmicpve.data.component.AccessoryItemData;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class BeltItemFactory {
    private BeltItemFactory() {}
    public static ItemStack create(BeltDefinition definition) {
        var item = switch (definition) {
            case SHOCK_THERAPY -> ModItems.SHOCK_THERAPY_BELT.get();
            case BANDOLIER -> ModItems.BANDOLIER_BELT.get();
            case JELLY_ROLL -> ModItems.JELLY_ROLL_BELT.get();
            case CINDERWOLF -> ModItems.CINDERWOLF_BELT.get();
        };
        var stack = new ItemStack(item);
        stack.set(ModDataComponents.ACCESSORY_ITEM.get(), new AccessoryItemData(
                AccessoryItemData.DATA_VERSION, AccessorySlot.BELT, definition.id()));
        return stack;
    }
}
