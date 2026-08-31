package com.cosmicpve.economy.flashsale;

import com.cosmicpve.economy.MoneyAmount;
import com.cosmicpve.economy.MoneyService;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class FlashSaleService {
    private final FlashSaleRepository repository;
    private final MoneyService money;
    private final RewardDeliveryService delivery;
    private final RandomSource random;

    public FlashSaleService() {
        this(new FlashSaleRepository(), new MoneyService(), new RewardDeliveryService(), RandomSource.create());
    }
    FlashSaleService(FlashSaleRepository repository, MoneyService money, RewardDeliveryService delivery, RandomSource random) {
        this.repository = repository; this.money = money; this.delivery = delivery; this.random = random;
    }

    public void tick(MinecraftServer server) {
        FlashSaleSavedData data = repository.data(server);
        long now = server.overworld().getGameTime();
        Optional<FlashSaleActive> active = data.active();
        if (active.isPresent()) {
            FlashSaleActive sale = active.orElseThrow();
            if (FlashSaleSchedule.expired(sale, now)) {
                data.setActive(Optional.empty());
                broadcast(server, Component.literal("The Flash Sale has ended!").withStyle(ChatFormatting.RED));
                return;
            }
            if (FlashSaleSchedule.reminderDue(sale, now)) {
                data.setActive(Optional.of(sale.withReminderSent()));
                FlashSaleCatalog.find(sale.entryId()).ifPresent(entry -> broadcast(server, reminder(entry, sale)));
            }
            return;
        }
        if (data.nextStartTick() == 0L) {
            data.setNextStartTick(now + FlashSaleSchedule.nextInterval(random::nextInt));
        } else if (now >= data.nextStartTick()) {
            startRandom(server);
        }
    }

    public Optional<FlashSaleActive> active(MinecraftServer server) { return repository.data(server).active(); }

    public boolean startRandom(MinecraftServer server) {
        FlashSaleEntry entry = FlashSaleSchedule.select(FlashSaleCatalog.productionRows(), random::nextInt);
        FlashSalePriceTier tier = FlashSaleSchedule.selectTier(random::nextInt);
        return start(server, entry, tier);
    }

    public boolean start(MinecraftServer server, FlashSaleEntry entry, FlashSalePriceTier tier) {
        if (!entry.productionSelectable()) return false;
        long now = server.overworld().getGameTime();
        var sale = new FlashSaleActive(entry.id(), tier, entry.price(tier), now,
                now + FlashSaleSchedule.DURATION, false, List.of());
        FlashSaleSavedData data = repository.data(server);
        data.setActive(Optional.of(sale));
        data.setNextStartTick(now + FlashSaleSchedule.nextInterval(random::nextInt));
        broadcast(server, startMessage(entry, sale));
        FlashSaleStartAlert.play(server.getPlayerList().getPlayers());
        return true;
    }

    public boolean close(MinecraftServer server) {
        FlashSaleSavedData data = repository.data(server);
        if (data.active().isEmpty()) return false;
        data.setActive(Optional.empty());
        broadcast(server, Component.literal("The Flash Sale has ended!").withStyle(ChatFormatting.RED));
        return true;
    }

    public boolean resetPurchasers(MinecraftServer server) {
        var active = repository.data(server).active();
        if (active.isEmpty()) return false;
        var sale = active.orElseThrow();
        repository.data(server).setActive(Optional.of(new FlashSaleActive(sale.entryId(), sale.priceTier(),
                sale.priceCents(), sale.startTick(), sale.endTick(), sale.reminderSent(), List.of())));
        return true;
    }

    public FlashSalePurchaseTransaction.Outcome buy(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        var active = repository.data(server).active();
        if (active.isEmpty() || FlashSaleSchedule.expired(active.orElseThrow(), server.overworld().getGameTime())) {
            player.sendSystemMessage(Component.literal("There is no current flash sale genius!"));
            return FlashSalePurchaseTransaction.Outcome.NO_SALE;
        }
        FlashSaleActive sale = active.orElseThrow();
        FlashSaleEntry entry = FlashSaleCatalog.find(sale.entryId()).orElse(null);
        var outcome = FlashSalePurchaseTransaction.execute(true, sale.purchased(player.getUUID()),
                money.balance(player), sale.priceCents(),
                () -> entry == null ? Optional.empty() : entry.create(random),
                amount -> money.subtract(player, amount),
                () -> repository.data(server).setActive(Optional.of(sale.withPurchaser(player.getUUID()))),
                rewards -> delivery.deliver(player, rewards));
        switch (outcome) {
            case SUCCESS -> player.sendSystemMessage(Component.literal("Purchased ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(entry.quantity() + "x ")).append(entry.displayName().copy())
                    .append(Component.literal(" for " + MoneyAmount.format(sale.priceCents()) + ".")
                            .withStyle(ChatFormatting.GREEN)));
            case ALREADY_PURCHASED -> player.sendSystemMessage(Component.literal("You already purchased this Flash Sale."));
            case INSUFFICIENT_FUNDS -> player.sendSystemMessage(Component.literal("You do not have enough money for this Flash Sale."));
            case REWARD_UNAVAILABLE -> player.sendSystemMessage(Component.literal("This Flash Sale reward is currently unavailable; nothing was charged."));
            case NO_SALE -> player.sendSystemMessage(Component.literal("There is no current flash sale genius!"));
        }
        return outcome;
    }

    public String status(MinecraftServer server) {
        var data = repository.data(server);
        if (data.active().isEmpty()) return "No active Flash Sale; next start tick=" + data.nextStartTick();
        var sale = data.active().orElseThrow();
        return sale.entryId() + " " + sale.priceTier().displayName() + " " + MoneyAmount.format(sale.priceCents())
                + " start=" + sale.startTick() + " end=" + sale.endTick() + " reminder=" + sale.reminderSent()
                + " purchasers=" + sale.purchasers().size() + " next=" + data.nextStartTick();
    }

    private Component startMessage(FlashSaleEntry entry, FlashSaleActive sale) {
        return commonMessage(entry, sale).append(Component.literal("\nType /buy to purchase!").withColor(0x55FFFF));
    }
    private Component reminder(FlashSaleEntry entry, FlashSaleActive sale) {
        return Component.literal("FLASH SALE! ").withStyle(style -> style.withColor(0xFFAA00).withBold(true))
                .append(Component.literal("1 minute remaining!\n").withStyle(style -> style.withColor(0xFF5555).withBold(true)))
                .append(offer(entry, sale)).append(Component.literal("\nType /buy to purchase!").withColor(0x55FFFF));
    }
    private MutableComponent commonMessage(FlashSaleEntry entry, FlashSaleActive sale) {
        return Component.literal("FLASH SALE! ").withStyle(style -> style.withColor(0xFFAA00).withBold(true))
                .append(offer(entry, sale));
    }
    private MutableComponent offer(FlashSaleEntry entry, FlashSaleActive sale) {
        return Component.literal(entry.quantity() + "x ").append(styledRewardName(entry))
                .append(Component.literal(" at the " + sale.priceTier().displayName() + " price of "))
                .append(Component.literal(MoneyAmount.format(sale.priceCents())).withStyle(style -> style.withColor(0x55FF55).withBold(true)))
                .append(Component.literal("!"));
    }
    private Component styledRewardName(FlashSaleEntry entry) {
        return entry.create(RandomSource.create(0L))
                .filter(stacks -> !stacks.isEmpty()).map(stacks -> stacks.getFirst().getHoverName().copy())
                .orElseGet(() -> entry.displayName().copy());
    }
    private static void broadcast(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
