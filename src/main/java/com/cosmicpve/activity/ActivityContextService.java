package com.cosmicpve.activity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

/** Narrow authoritative activity query; future activity sessions own setting and clearing it. */
public final class ActivityContextService {
    private final Map<UUID, ActivityType> active = new ConcurrentHashMap<>();
    public ActivityType current(LivingEntity entity) { return active.getOrDefault(entity.getUUID(), ActivityType.NONE); }
    public boolean isDungeon(LivingEntity entity) { return current(entity) == ActivityType.DUNGEON; }
    public void set(UUID entityId, ActivityType type) {
        if (type == ActivityType.NONE) active.remove(entityId); else active.put(entityId, type);
    }
    public void clear(UUID entityId) { active.remove(entityId); }
}
