package com.cosmicpve.equipment.enchantment;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Session-local, item-preserving suppression of selected Cosmic enchantment tiers. */
public final class EnchantmentSuppressionService {
    public static final EnchantmentSuppressionService GLOBAL = new EnchantmentSuppressionService();
    private final Map<LivingEntity, Window> windows = Collections.synchronizedMap(new WeakHashMap<>());

    public void suppress(LivingEntity target, Set<CosmicEnchantmentTier> tiers, int durationTicks, long currentTick) {
        if (durationTicks <= 0 || tiers.isEmpty() || target.isDeadOrDying()) return;
        var copy = EnumSet.copyOf(tiers);
        synchronized (windows) {
            Window old = windows.get(target);
            long expiry = refreshedExpiry(Long.MIN_VALUE, currentTick, durationTicks);
            if (old != null && old.expiryTick() > currentTick) {
                copy.addAll(old.tiers());
                expiry = refreshedExpiry(old.expiryTick(), currentTick, durationTicks);
            }
            windows.put(target, new Window(Set.copyOf(copy), expiry));
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
        Window window = windows.get(entity);
        if (window == null) return false;
        if (currentTick >= window.expiryTick()) {
            windows.remove(entity);
            return false;
        }
        return CosmicEnchantmentSpecs.find(enchantmentId)
                .map(spec -> window.tiers().contains(spec.tier())).orElse(false);
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

    private record Window(Set<CosmicEnchantmentTier> tiers, long expiryTick) {}
}
