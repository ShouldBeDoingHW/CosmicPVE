package com.cosmicpve.economy.flashsale;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

/** Small exactly-once ordering seam; the server adapter supplies persistent balance/purchaser mutations. */
public final class FlashSalePurchaseTransaction {
    private FlashSalePurchaseTransaction() {}

    public static Outcome execute(boolean active, boolean alreadyPurchased, long balance, long price,
            Supplier<Optional<List<ItemStack>>> construction, LongPredicate debit,
            Runnable markPurchased, Consumer<List<ItemStack>> delivery) {
        if (!active) return Outcome.NO_SALE;
        if (alreadyPurchased) return Outcome.ALREADY_PURCHASED;
        Optional<List<ItemStack>> reward = construction.get();
        if (reward.isEmpty() || reward.orElseThrow().isEmpty()
                || reward.orElseThrow().stream().anyMatch(ItemStack::isEmpty)) return Outcome.REWARD_UNAVAILABLE;
        if (price < 1 || balance < price || !debit.test(price)) return Outcome.INSUFFICIENT_FUNDS;
        markPurchased.run();
        delivery.accept(reward.orElseThrow());
        return Outcome.SUCCESS;
    }

    public enum Outcome { NO_SALE, ALREADY_PURCHASED, REWARD_UNAVAILABLE, INSUFFICIENT_FUNDS, SUCCESS }
}
