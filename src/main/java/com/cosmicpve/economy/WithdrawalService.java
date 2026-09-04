package com.cosmicpve.economy;

import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative one-note withdrawal transaction. */
public final class WithdrawalService {
    private final MoneyService money;
    private final RewardDeliveryService delivery;

    public WithdrawalService() {
        this(new MoneyService(), new RewardDeliveryService());
    }

    WithdrawalService(MoneyService money, RewardDeliveryService delivery) {
        this.money = money;
        this.delivery = delivery;
    }

    public Result withdraw(ServerPlayer player, String raw) {
        long balance = money.balance(player);
        Resolution resolution = resolve(raw, balance);
        if (resolution.status() != Status.SUCCESS) return new Result(resolution.status(), 0L);

        ItemStack note;
        try {
            note = Banknotes.create(resolution.cents());
        } catch (RuntimeException invalid) {
            return new Result(Status.INVALID, 0L);
        }
        if (note.isEmpty() || !money.subtract(player, resolution.cents())) {
            return new Result(Status.INSUFFICIENT_FUNDS, 0L);
        }
        // placeItemBackInInventory is the accepted safe-delivery path: it inserts what fits and
        // drops the exact remainder at the player, so a full inventory cannot lose the note.
        delivery.deliver(player, List.of(note));
        return new Result(Status.SUCCESS, resolution.cents());
    }

    public static Resolution resolve(String raw, long balance) {
        if (balance < 0 || raw == null) return new Resolution(Status.INVALID, 0L);
        String value = raw.trim();
        if (value.equalsIgnoreCase("all") || value.equalsIgnoreCase("max")) {
            return balance == 0
                    ? new Resolution(Status.INSUFFICIENT_FUNDS, 0L)
                    : new Resolution(Status.SUCCESS, balance);
        }
        final long cents;
        try {
            cents = MoneyAmount.parseCents(value);
        } catch (IllegalArgumentException invalid) {
            return new Resolution(Status.INVALID, 0L);
        }
        return cents > balance
                ? new Resolution(Status.INSUFFICIENT_FUNDS, 0L)
                : new Resolution(Status.SUCCESS, cents);
    }

    public enum Status { SUCCESS, INVALID, INSUFFICIENT_FUNDS }
    public record Resolution(Status status, long cents) {}
    public record Result(Status status, long cents) {
        public boolean succeeded() { return status == Status.SUCCESS; }
    }
}
