package com.cosmicpve.spacechest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

final class SpaceChestRuntime {
    final SpaceChestTier tier;
    final List<List<ItemStack>> rewards = new ArrayList<>(27);
    int revealTicks;
    long revealStartedAtTick;
    boolean revealFinished;
    SpaceChestRuntime(SpaceChestTier tier) { this.tier = tier; }
}
