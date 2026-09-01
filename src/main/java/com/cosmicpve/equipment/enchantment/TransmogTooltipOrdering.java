package com.cosmicpve.equipment.enchantment;

import java.util.Comparator;
import net.minecraft.resources.Identifier;

/** Deterministic presentation-only ordering used when an item has been Transmog-sorted. */
public final class TransmogTooltipOrdering {
    public record Key(boolean cosmic, CosmicEnchantmentTier tier, int level, Identifier id, int originalIndex) {}
    private TransmogTooltipOrdering() {}

    public static final Comparator<Key> COMPARATOR = Comparator
            .comparingInt(TransmogTooltipOrdering::category)
            .thenComparing((Key key) -> key.cosmic() ? -key.level() : key.originalIndex())
            .thenComparing(key -> key.id() == null ? "" : key.id().toString());

    private static int category(Key key) {
        if (!key.cosmic()) return 0;
        return switch (key.tier()) {
            case MASTERY -> 1;
            case HEROIC -> 2;
            case LEGENDARY -> 3;
            case ULTIMATE -> 4;
            case ELITE -> 5;
            case UNIQUE -> 6;
            case SIMPLE -> 7;
        };
    }
}
