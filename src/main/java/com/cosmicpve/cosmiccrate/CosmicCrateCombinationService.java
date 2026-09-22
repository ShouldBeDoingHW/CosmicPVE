package com.cosmicpve.cosmiccrate;

import net.minecraft.world.item.ItemStack;

public final class CosmicCrateCombinationService {
    public enum Outcome { SUCCESS, REJECTED_STALE_TARGET, REJECTED_INVALID }
    public Outcome combine(ItemStack carried, ItemStack expectedTarget, ItemStack target) {
        if (expectedTarget != target) return Outcome.REJECTED_STALE_TARGET;
        if (!(carried.getItem() instanceof CosmicCrateHalfItem first)
                || !(target.getItem() instanceof CosmicCrateHalfItem second)
                || first.season() != second.season() || first.side() == second.side()
                || carried.getCount() != 1 || target.getCount() != 1) return Outcome.REJECTED_INVALID;
        carried.setCount(0);
        target.setCount(0);
        return Outcome.SUCCESS;
    }
}
