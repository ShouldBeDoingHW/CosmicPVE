package com.cosmicpve.economy.fame;

import com.cosmicpve.trial.TrialPhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record FameShopOffer(TrialPhase sourceTier, String sourceRow, List<ItemStack> payload, long price, boolean purchased) {
    public static final Codec<FameShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrialPhase.CODEC.fieldOf("source_tier").forGetter(FameShopOffer::sourceTier),
            Codec.STRING.fieldOf("source_row").forGetter(FameShopOffer::sourceRow),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("payload").forGetter(FameShopOffer::payload),
            Codec.LONG.fieldOf("price").forGetter(FameShopOffer::price),
            Codec.BOOL.optionalFieldOf("purchased", false).forGetter(FameShopOffer::purchased)
    ).apply(instance, FameShopOffer::new));
    public FameShopOffer {
        if (sourceRow.isBlank()) throw new IllegalArgumentException("Missing Fame offer source row");
        payload = payload.stream().map(ItemStack::copy).toList();
        if (payload.isEmpty() || price <= 0) throw new IllegalArgumentException("Invalid Fame offer");
    }
    public FameShopOffer markPurchased() { return new FameShopOffer(sourceTier, sourceRow, payload, price, true); }
}
