package com.cosmicpve.equipment.accessory;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;

/** Server-runtime, non-persistent combat state. Re-triggers replace expiry and never stack magnitude. */
public final class BlackHeartStateService {
    public static final long DURATION_TICKS = 80L;
    private final Map<LivingEntity, Long> expiry = java.util.Collections.synchronizedMap(new WeakHashMap<>());
    public void activate(LivingEntity entity, long currentTick) { expiry.put(entity, currentTick + DURATION_TICKS); }
    public boolean active(LivingEntity entity, long currentTick) {
        var value = expiry.get(entity);
        if (value == null) return false;
        if (currentTick >= value) { expiry.remove(entity); return false; }
        return true;
    }
    public long expiryTick(LivingEntity entity) { return expiry.getOrDefault(entity, 0L); }
}
