package com.cosmicpve.equipment.enchantment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Session-local, item-preserving, source-composable Cosmic enchantment suppression. */
public final class EnchantmentSuppressionService {
    public static final EnchantmentSuppressionService GLOBAL = new EnchantmentSuppressionService();
    public static final Identifier LEGACY_TIER_SOURCE = Identifier.fromNamespaceAndPath("cosmicpve", "tier_suppression");
    private final Map<LivingEntity, Map<Key, Long>> windows = Collections.synchronizedMap(new WeakHashMap<>());

    public void suppress(LivingEntity target, Set<CosmicEnchantmentTier> tiers, int durationTicks, long currentTick) {
        suppressTiers(target, LEGACY_TIER_SOURCE, tiers, durationTicks, currentTick);
    }

    public void suppressTiers(LivingEntity target, Identifier source, Set<CosmicEnchantmentTier> tiers,
            int durationTicks, long currentTick) {
        if (durationTicks <= 0 || tiers.isEmpty() || target.isDeadOrDying()) return;
        synchronized (windows) {
            var entries = windows.computeIfAbsent(target, ignored -> new HashMap<>());
            tiers.forEach(tier -> entries.put(Key.tier(source, tier), currentTick + durationTicks));
        }
    }

    public void suppressEnchantment(LivingEntity target, Identifier source, Identifier enchantmentId,
            int durationTicks, long currentTick) {
        if (durationTicks <= 0 || target.isDeadOrDying()) return;
        synchronized (windows) {
            windows.computeIfAbsent(target, ignored -> new HashMap<>())
                    .put(Key.enchantment(source, enchantmentId), currentTick + durationTicks);
        }
    }

    public boolean isSuppressed(LivingEntity entity, Identifier enchantmentId) {
        var server = entity.level().getServer();
        return server != null && isSuppressed(entity, enchantmentId, server.getTickCount());
    }

    public boolean isSuppressed(LivingEntity entity, Identifier enchantmentId, long currentTick) {
        if (entity.isDeadOrDying() || entity.isRemoved()) {
            windows.remove(entity);
            return false;
        }
        return isSuppressedExceptSource(entity, enchantmentId, null, currentTick);
    }

    public boolean isSuppressedExceptSource(LivingEntity entity, Identifier enchantmentId,
            Identifier ignoredSource, long currentTick) {
        if (entity.isDeadOrDying() || entity.isRemoved()) { windows.remove(entity); return false; }
        synchronized (windows) {
            var entries = windows.get(entity);
            if (entries == null) return false;
            entries.entrySet().removeIf(entry -> currentTick >= entry.getValue());
            if (entries.isEmpty()) { windows.remove(entity); return false; }
            var tier = CosmicEnchantmentSpecs.find(enchantmentId).map(CosmicEnchantmentSpec::tier).orElse(null);
            return entries.keySet().stream().anyMatch(key -> !key.source().equals(ignoredSource)
                    && (enchantmentId.equals(key.enchantmentId()) || tier == key.tier()));
        }
    }

    public boolean isSuppressedBySource(LivingEntity entity, Identifier enchantmentId,
            Identifier source, long currentTick) {
        synchronized (windows) {
            var entries = windows.get(entity);
            if (entries == null) return false;
            entries.entrySet().removeIf(entry -> currentTick >= entry.getValue());
            return entries.keySet().stream().anyMatch(key -> key.source().equals(source)
                    && enchantmentId.equals(key.enchantmentId()));
        }
    }

    public static boolean suppressesTier(Set<CosmicEnchantmentTier> tiers, CosmicEnchantmentTier tier) {
        return tiers.contains(tier);
    }

    public static long refreshedExpiry(long existingExpiry, long currentTick, int durationTicks) {
        if (durationTicks <= 0) throw new IllegalArgumentException("Suppression duration must be positive");
        return Math.max(existingExpiry, currentTick + durationTicks);
    }

    public static boolean activeAt(long expiryTick, long currentTick) {
        return currentTick < expiryTick;
    }

    private record Key(Identifier source, CosmicEnchantmentTier tier, Identifier enchantmentId) {
        static Key tier(Identifier source, CosmicEnchantmentTier tier) { return new Key(source, tier, null); }
        static Key enchantment(Identifier source, Identifier id) { return new Key(source, null, id); }
    }
}
