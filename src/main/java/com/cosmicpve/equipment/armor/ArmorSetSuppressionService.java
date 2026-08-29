package com.cosmicpve.equipment.armor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;

/** Ephemeral server-authoritative suppression of an otherwise active armor-set bonus. */
public final class ArmorSetSuppressionService {
    private final Map<LivingEntity, Long> suppressedUntil = Collections.synchronizedMap(new WeakHashMap<>());

    public void suppress(LivingEntity entity, int durationTicks, long currentTick) {
        if (entity.level().isClientSide() || durationTicks < 1) return;
        suppressedUntil.put(entity, currentTick + durationTicks);
    }

    public boolean isSuppressed(LivingEntity entity) {
        var server = entity.level().getServer();
        return server != null && isSuppressed(entity, server.getTickCount());
    }

    public boolean isSuppressed(LivingEntity entity, long currentTick) {
        Long expiry = suppressedUntil.get(entity);
        if (expiry == null) return false;
        if (expiry <= currentTick || entity.isRemoved() || entity.isDeadOrDying()) {
            suppressedUntil.remove(entity);
            return false;
        }
        return true;
    }

    public long remainingTicks(LivingEntity entity, long currentTick) {
        return isSuppressed(entity, currentTick) ? suppressedUntil.get(entity) - currentTick : 0L;
    }
}
