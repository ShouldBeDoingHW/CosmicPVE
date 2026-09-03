package com.cosmicpve.enchanter;

import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

/** Small ordering seam: construct, debit exactly once, then deliver. */
public final class EnchanterPurchaseTransaction {
    private EnchanterPurchaseTransaction() {}

    public static Outcome execute(int balance, int cost, Supplier<ItemStack> construction,
            IntPredicate debit, Consumer<ItemStack> delivery) {
        if (cost <= 0 || balance < cost) return Outcome.INSUFFICIENT_XP;
        ItemStack reward = construction.get();
        if (reward == null || reward.isEmpty()) return Outcome.REWARD_UNAVAILABLE;
        if (!debit.test(cost)) return Outcome.DEBIT_REJECTED;
        delivery.accept(reward);
        return Outcome.SUCCESS;
    }

    public enum Outcome { SUCCESS, INSUFFICIENT_XP, REWARD_UNAVAILABLE, DEBIT_REJECTED }
}
