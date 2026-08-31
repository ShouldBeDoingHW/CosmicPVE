package com.cosmicpve.economy.flashsale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record FlashSaleActive(String entryId, FlashSalePriceTier priceTier, long priceCents,
        long startTick, long endTick, boolean reminderSent, List<UUID> purchasers) {
    public static final Codec<FlashSaleActive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("entry_id").forGetter(FlashSaleActive::entryId),
            FlashSalePriceTier.CODEC.fieldOf("price_tier").forGetter(FlashSaleActive::priceTier),
            Codec.LONG.fieldOf("price_cents").forGetter(FlashSaleActive::priceCents),
            Codec.LONG.fieldOf("start_tick").forGetter(FlashSaleActive::startTick),
            Codec.LONG.fieldOf("end_tick").forGetter(FlashSaleActive::endTick),
            Codec.BOOL.optionalFieldOf("reminder_sent", false).forGetter(FlashSaleActive::reminderSent),
            UUIDUtil.CODEC.listOf().optionalFieldOf("purchasers", List.of()).forGetter(FlashSaleActive::purchasers)
    ).apply(instance, FlashSaleActive::new));

    public FlashSaleActive {
        if (entryId == null || entryId.isBlank() || priceCents < 1 || startTick < 0 || endTick <= startTick)
            throw new IllegalArgumentException("Invalid active Flash Sale");
        purchasers = List.copyOf(new java.util.LinkedHashSet<>(purchasers));
    }

    public boolean purchased(UUID player) { return purchasers.contains(player); }
    public FlashSaleActive withReminderSent() {
        return reminderSent ? this : new FlashSaleActive(entryId, priceTier, priceCents, startTick, endTick, true, purchasers);
    }
    public FlashSaleActive withPurchaser(UUID player) {
        if (purchased(player)) return this;
        var updated = new java.util.ArrayList<>(purchasers); updated.add(player);
        return new FlashSaleActive(entryId, priceTier, priceCents, startTick, endTick, reminderSent, updated);
    }
}
