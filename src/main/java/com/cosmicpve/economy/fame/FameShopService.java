package com.cosmicpve.economy.fame;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.content.definition.reward.RewardEntry;
import com.cosmicpve.economy.FameService;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import com.cosmicpve.trial.TrialPhase;
import com.cosmicpve.trial.TrialSessionService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Persistent per-player nine-offer catalog built from raw constructible Trial rows. */
public final class FameShopService {
    public static final int OFFER_COUNT = 9;
    public static final long REFRESH_DAYS = 10L;
    private static final List<SourceTable> SOURCES = List.of(
            new SourceTable(TrialPhase.APPRENTICE, TrialSessionService.APPRENTICE_REWARDS, priceFor(TrialPhase.APPRENTICE)),
            new SourceTable(TrialPhase.HARDCORE, TrialSessionService.HARDCORE_REWARDS, priceFor(TrialPhase.HARDCORE)),
            new SourceTable(TrialPhase.IMPOSSIBLE, TrialSessionService.IMPOSSIBLE_REWARDS, priceFor(TrialPhase.IMPOSSIBLE)),
            new SourceTable(TrialPhase.DEMONIC, TrialSessionService.DEMONIC_REWARDS, priceFor(TrialPhase.DEMONIC)));
    private final RewardGeneratorService generator = new RewardGeneratorService();
    private final RewardDeliveryService delivery = new RewardDeliveryService();
    private final FameService fame = new FameService();

    public FameShopCatalog catalog(ServerPlayer player) {
        long day = currentDay(player);
        FameShopCatalog current = player.getData(ModAttachments.FAME_SHOP);
        if (current.current(day)) return current;
        FameShopCatalog generated = generate(player, day, RandomSource.create());
        player.setData(ModAttachments.FAME_SHOP, generated);
        return generated;
    }

    FameShopCatalog generate(ServerPlayer player, long day, RandomSource random) {
        var rows = new ArrayList<SourceRow>();
        for (SourceTable source : SOURCES) {
            List<RewardEntry> entries = CosmicContent.repository().requireRewardTable(source.table()).entries();
            for (int index = 0; index < entries.size(); index++) {
                rows.add(new SourceRow(source, index, entries.get(index)));
            }
        }
        int total = rows.stream().mapToInt(row -> row.entry().weight()).sum();
        var offers = new ArrayList<FameShopOffer>();
        for (int attempts = 0; offers.size() < OFFER_COUNT && attempts < 1_000; attempts++) {
            int pick = random.nextInt(total), cursor = 0; SourceRow selected = null;
            for (SourceRow row : rows) { cursor += row.entry().weight(); if (pick < cursor) { selected = row; break; } }
            if (selected == null) throw new IllegalStateException("Fame Shop aggregate selector exhausted");
            List<ItemStack> payload = materialize(selected.entry(), player, random);
            if (payload.isEmpty() || offers.stream().anyMatch(existing -> samePayload(existing.payload(), payload))) continue;
            offers.add(new FameShopOffer(selected.source().phase(), selected.identity(), payload,
                    selected.source().price(), false));
        }
        if (offers.size() != OFFER_COUNT) throw new IllegalStateException("Could not materialize nine distinct Fame Shop offers");
        return new FameShopCatalog(FameShopCatalog.DATA_VERSION, day, Math.addExact(day, REFRESH_DAYS), offers);
    }

    private List<ItemStack> materialize(RewardEntry entry, ServerPlayer player, RandomSource random) {
        int quantity = random.nextIntBetweenInclusive(entry.minimumQuantity(), entry.maximumQuantity());
        var generated = new ArrayList<ItemStack>();
        var context = new RewardGenerationContext(player.registryAccess(), random, player);
        for (int i = 0; i < quantity; i++) generator.generate(entry.reward(), context).ifPresent(generated::add);
        return RewardTableService.compact(generated);
    }

    public PurchaseResult purchase(ServerPlayer player, int slot) {
        long day = currentDay(player);
        FameShopCatalog catalog = player.getData(ModAttachments.FAME_SHOP);
        if (!catalog.current(day)) { catalog(player); return PurchaseResult.EXPIRED; }
        if (slot < 0 || slot >= catalog.offers().size()) return PurchaseResult.INVALID;
        FameShopOffer offer = catalog.offers().get(slot);
        if (offer.purchased()) return PurchaseResult.PURCHASED;
        if (fame.balance(player) < offer.price()) return PurchaseResult.INSUFFICIENT;
        if (!fame.subtract(player, offer.price())) return PurchaseResult.INSUFFICIENT;
        player.setData(ModAttachments.FAME_SHOP, catalog.purchase(slot));
        delivery.deliver(player, offer.payload());
        return PurchaseResult.SUCCESS;
    }

    public FameShopCatalog forceRefresh(ServerPlayer player) {
        long day = currentDay(player);
        FameShopCatalog generated = generate(player, day, RandomSource.create());
        player.setData(ModAttachments.FAME_SHOP, generated); return generated;
    }
    public static long currentDay(ServerPlayer player) { return Math.floorDiv(player.level().getServer().overworld().getDayTime(), 24_000L); }
    public static boolean samePayload(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) if (first.get(i).getCount() != second.get(i).getCount()
                || !ItemStack.isSameItemSameComponents(first.get(i), second.get(i))) return false;
        return true;
    }
    public static long priceFor(TrialPhase phase) {
        return switch (phase) { case APPRENTICE -> 20; case HARDCORE -> 35; case IMPOSSIBLE -> 55; case DEMONIC -> 75; };
    }
    public enum PurchaseResult { SUCCESS, INSUFFICIENT, PURCHASED, EXPIRED, INVALID }
    private record SourceTable(TrialPhase phase, net.minecraft.resources.Identifier table, long price) {}
    private record SourceRow(SourceTable source, int index, RewardEntry entry) {
        private String identity() { return source.table() + "#" + index; }
    }
}
