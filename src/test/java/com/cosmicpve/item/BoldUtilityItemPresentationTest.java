package com.cosmicpve.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class BoldUtilityItemPresentationTest {
    @Test
    void transmogAndRepairScrollNamesAreBold() {
        assertTrue(new ItemStack(ModItems.TRANSMOG_SCROLL.get()).getHoverName().getStyle().isBold());
        assertTrue(new ItemStack(ModItems.REPAIR_SCROLL.get()).getHoverName().getStyle().isBold());
    }
}
