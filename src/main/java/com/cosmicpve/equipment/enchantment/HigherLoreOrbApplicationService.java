package com.cosmicpve.equipment.enchantment;

import net.minecraft.world.item.ItemStack;

public final class HigherLoreOrbApplicationService {
    public enum Outcome { SUCCESS, REJECTED_INVALID_ORB, REJECTED_TARGET, REJECTED_CAPACITY, STALE_TARGET }
    private final CustomEnchantCapacityService capacity;
    public HigherLoreOrbApplicationService(CustomEnchantCapacityService capacity) { this.capacity = capacity; }
    public Outcome apply(ItemStack orb, ItemStack expected, ItemStack target) {
        if (expected != target) return Outcome.STALE_TARGET;
        if (!(orb.getItem() instanceof HigherLoreOrbItem higher)) return Outcome.REJECTED_INVALID_ORB;
        if (!higher.type().matches(target)) return Outcome.REJECTED_TARGET;
        if (capacity.capacity(target) != higher.requiredCapacity()) return Outcome.REJECTED_CAPACITY;
        if (!capacity.unlockTo(target, higher.destinationCapacity())) return Outcome.REJECTED_CAPACITY;
        orb.shrink(1);
        return Outcome.SUCCESS;
    }
}
