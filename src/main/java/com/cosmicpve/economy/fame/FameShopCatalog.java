package com.cosmicpve.economy.fame;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record FameShopCatalog(int dataVersion, long generatedDay, long nextRefreshDay, List<FameShopOffer> offers) {
    public static final int DATA_VERSION = 1;
    public static final com.mojang.serialization.MapCodec<FameShopCatalog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(FameShopCatalog::dataVersion),
            com.mojang.serialization.Codec.LONG.optionalFieldOf("generated_day", 0L).forGetter(FameShopCatalog::generatedDay),
            com.mojang.serialization.Codec.LONG.optionalFieldOf("next_refresh_day", 0L).forGetter(FameShopCatalog::nextRefreshDay),
            FameShopOffer.CODEC.listOf().optionalFieldOf("offers", List.of()).forGetter(FameShopCatalog::offers)
    ).apply(instance, FameShopCatalog::new));
    public FameShopCatalog { offers = List.copyOf(offers); }
    public static FameShopCatalog empty() { return new FameShopCatalog(DATA_VERSION, 0L, 0L, List.of()); }
    public boolean current(long day) { return offers.size() == 9 && day < nextRefreshDay; }
    public FameShopCatalog purchase(int slot) {
        var next = new java.util.ArrayList<>(offers);
        next.set(slot, next.get(slot).markPurchased());
        return new FameShopCatalog(dataVersion, generatedDay, nextRefreshDay, next);
    }
}
