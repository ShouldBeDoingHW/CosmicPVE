package com.cosmicpve.equipment.accessory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

/** Transient committed-hit state; deliberately never serialized. */
public final class BandolierStateService {
    private final Map<UUID, Integer> hits = new ConcurrentHashMap<>();
    public int count(LivingEntity entity) { return hits.getOrDefault(entity.getUUID(), 0); }
    public boolean charged(LivingEntity entity) { return count(entity) == 3; }
    public void committed(LivingEntity entity) { hits.compute(entity.getUUID(), (id, value) -> value == null ? 1 : value >= 3 ? 0 : value + 1); }
    public void reset(LivingEntity entity) { hits.remove(entity.getUUID()); }
}
