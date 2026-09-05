package com.cosmicpve.activity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;

/** Narrow authoritative activity query; future activity sessions own setting and clearing it. */
public final class ActivityContextService {
    private final Map<UUID, ActivityType> active = new ConcurrentHashMap<>();
    private final Set<UUID> dungeonParkour = ConcurrentHashMap.newKeySet();
    public ActivityType current(LivingEntity entity) { return active.getOrDefault(entity.getUUID(), ActivityType.NONE); }
    public boolean isDungeon(LivingEntity entity) { return current(entity) == ActivityType.DUNGEON; }
    public boolean isDungeonParkour(LivingEntity entity) {
        return isDungeon(entity) && dungeonParkour.contains(entity.getUUID());
    }
    public boolean isDungeonParkour(UUID entityId) {
        return active.getOrDefault(entityId, ActivityType.NONE) == ActivityType.DUNGEON
                && dungeonParkour.contains(entityId);
    }
    public void set(UUID entityId, ActivityType type) {
        if (type == ActivityType.NONE) active.remove(entityId); else active.put(entityId, type);
        // A generic activity assignment never implies the narrower parkour phase.
        dungeonParkour.remove(entityId);
    }
    /** Future Dungeon sessions call this authoritative seam only for their parkour phase. */
    public void setDungeonParkour(UUID entityId, boolean enabled) {
        if (enabled) {
            active.put(entityId, ActivityType.DUNGEON);
            dungeonParkour.add(entityId);
        } else dungeonParkour.remove(entityId);
    }
    public void clear(UUID entityId) { active.remove(entityId); dungeonParkour.remove(entityId); }
}
