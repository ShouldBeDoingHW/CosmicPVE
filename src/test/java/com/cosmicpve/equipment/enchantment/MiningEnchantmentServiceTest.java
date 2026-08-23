package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class MiningEnchantmentServiceTest {
    @Test void experienceUsesFinalVanillaXpAndFloorsFractionalResults() {
        assertEquals(6, MiningEnchantmentService.scaleBlockExperience(4, 1));
        assertEquals(8, MiningEnchantmentService.scaleBlockExperience(4, 2));
        assertEquals(10, MiningEnchantmentService.scaleBlockExperience(4, 3));
        assertEquals(4, MiningEnchantmentService.scaleBlockExperience(3, 1));
        assertEquals(0, MiningEnchantmentService.scaleBlockExperience(0, 3));
        assertEquals(4, MiningEnchantmentService.scaleBlockExperience(4, 0));
    }

    @Test void autoSmeltTransformsFinalDropsOnceAndPreservesGeneratedQuantity() {
        var result = MiningEnchantmentService.transformStacks(
                List.of(new ItemStack(Items.RAW_IRON, 3), new ItemStack(Items.COBBLESTONE, 2)),
                stack -> stack.is(Items.RAW_IRON) ? new ItemStack(Items.IRON_INGOT) : ItemStack.EMPTY);
        assertEquals(2, result.size());
        assertEquals(Items.IRON_INGOT, result.get(0).getItem());
        assertEquals(3, result.get(0).getCount());
        assertEquals(Items.COBBLESTONE, result.get(1).getItem());
        assertEquals(2, result.get(1).getCount());
    }

    @Test void multiOutputRecipesMultiplyAndSplitLegally() {
        var result = MiningEnchantmentService.transformStacks(List.of(new ItemStack(Items.RAW_IRON, 40)),
                ignored -> new ItemStack(Items.IRON_INGOT, 2));
        assertEquals(2, result.size());
        assertEquals(64, result.get(0).getCount());
        assertEquals(16, result.get(1).getCount());
    }
}
