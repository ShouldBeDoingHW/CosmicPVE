package com.cosmicpve.spacechest;

import com.cosmicpve.data.component.SpaceChestData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class SpaceChests {
    private SpaceChests() {}
    public static ItemStack create(SpaceChestTier tier) {
        return createOne(tier);
    }

    public static List<ItemStack> createMany(SpaceChestTier tier, int count) {
        if (count < 1) throw new IllegalArgumentException("count must be positive");
        var stacks = new ArrayList<ItemStack>(count);
        for (int index = 0; index < count; index++) stacks.add(createOne(tier));
        return List.copyOf(stacks);
    }

    private static ItemStack createOne(SpaceChestTier tier) {
        ItemStack stack = new ItemStack(ModItems.SPACE_CHEST.get());
        stack.set(ModDataComponents.SPACE_CHEST.get(), new SpaceChestData(tier));
        return stack;
    }
}
