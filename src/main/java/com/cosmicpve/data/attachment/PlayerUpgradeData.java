package com.cosmicpve.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Versioned, UUID-bound banked crystals and purchased player-upgrade tiers. */
public record PlayerUpgradeData(int dataVersion, long bankedCrystals, Map<Identifier, Integer> tiers) {
    public static final int CURRENT_DATA_VERSION = 1;
    private static final Codec<Map<Identifier, Integer>> TIERS = Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, 5));
    public static final com.mojang.serialization.MapCodec<PlayerUpgradeData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION).forGetter(PlayerUpgradeData::dataVersion),
            Codec.LONG.optionalFieldOf("banked_crystals", 0L).forGetter(PlayerUpgradeData::bankedCrystals),
            TIERS.optionalFieldOf("tiers", Map.of()).forGetter(PlayerUpgradeData::tiers)
    ).apply(instance, PlayerUpgradeData::new));

    public PlayerUpgradeData {
        if (bankedCrystals < 0) throw new IllegalArgumentException("Banked Upgrade Crystals cannot be negative");
        tiers = Map.copyOf(tiers);
    }

    public static PlayerUpgradeData empty() { return new PlayerUpgradeData(CURRENT_DATA_VERSION, 0L, Map.of()); }
    public int tier(Identifier id) { return tiers.getOrDefault(id, 0); }
}
