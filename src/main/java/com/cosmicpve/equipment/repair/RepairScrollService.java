package com.cosmicpve.equipment.repair;

import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class RepairScrollService {
    public enum Outcome { SUCCESS, INVALID_SCROLL, STALE_TARGET, NOT_DAMAGEABLE, ALREADY_REPAIRED }

    public Outcome apply(ItemStack scroll, ItemStack expectedTarget, ItemStack authoritativeTarget) {
        if (!scroll.is(ModItems.REPAIR_SCROLL.get())) return Outcome.INVALID_SCROLL;
        if (expectedTarget != authoritativeTarget) return Outcome.STALE_TARGET;
        if (!authoritativeTarget.isDamageableItem()) return Outcome.NOT_DAMAGEABLE;
        if (authoritativeTarget.getDamageValue() <= 0) return Outcome.ALREADY_REPAIRED;
        authoritativeTarget.setDamageValue(0);
        scroll.shrink(1);
        return Outcome.SUCCESS;
    }
}
