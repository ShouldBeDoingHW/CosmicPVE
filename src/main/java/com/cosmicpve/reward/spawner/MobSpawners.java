package com.cosmicpve.reward.spawner;

import com.cosmicpve.data.component.MobSpawnerData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class MobSpawners {
    private MobSpawners() {}

    public static ItemStack create(Identifier entityTypeId, int count) {
        if (count < 1) throw new IllegalArgumentException("count must be positive");
        ItemStack stack = new ItemStack(ModItems.MOB_SPAWNER.get(), count);
        stack.set(ModDataComponents.MOB_SPAWNER.get(),
                new MobSpawnerData(MobSpawnerData.CURRENT_DATA_VERSION, entityTypeId));
        return stack;
    }
}
