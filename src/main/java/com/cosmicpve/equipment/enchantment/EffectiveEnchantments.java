package com.cosmicpve.equipment.enchantment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class EffectiveEnchantments {
    public static final EffectiveEnchantments EMPTY = new EffectiveEnchantments(Map.of());
    private final Map<Identifier, EffectiveEnchantment> entries;

    public EffectiveEnchantments(Map<Identifier, EffectiveEnchantment> entries) {
        var ordered = new LinkedHashMap<Identifier, EffectiveEnchantment>();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(Identifier::toString)))
                .forEach(entry -> ordered.put(entry.getKey(), entry.getValue()));
        this.entries = Collections.unmodifiableMap(ordered);
    }

    public int level(Identifier enchantmentId) {
        return Optional.ofNullable(entries.get(enchantmentId)).map(EffectiveEnchantment::level).orElse(0);
    }

    public Optional<EffectiveEnchantment> get(Identifier enchantmentId) {
        return Optional.ofNullable(entries.get(enchantmentId));
    }

    public Collection<EffectiveEnchantment> entries() {
        return entries.values();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
