package com.cosmicpve.equipment.enchantment;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record EffectiveEnchantment(Identifier id, int level, List<EnchantmentProvenance> provenance) {
    private static final Comparator<EnchantmentProvenance> SOURCE_ORDER = Comparator
            .comparing(EnchantmentProvenance::kind)
            .thenComparing(source -> source.sourceId().toString())
            .thenComparingInt(EnchantmentProvenance::level);

    public EffectiveEnchantment {
        id = Objects.requireNonNull(id);
        if (level <= 0) {
            throw new IllegalArgumentException("Effective enchantment level must be positive");
        }
        provenance = provenance.stream().sorted(SOURCE_ORDER).toList();
    }

    public boolean hasVirtualSource() {
        return provenance.stream().anyMatch(source -> source.kind() == EnchantmentSourceKind.VIRTUAL);
    }
}
