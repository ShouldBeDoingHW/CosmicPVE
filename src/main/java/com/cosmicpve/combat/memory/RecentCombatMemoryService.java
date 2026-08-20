package com.cosmicpve.combat.memory;

import com.cosmicpve.combat.api.CombatResult;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

/** Bounded server-tick history of committed damaging relationships. */
public final class RecentCombatMemoryService {
    private final long retentionTicks;
    private final Map<Relationship, ArrayDeque<Long>> history = new HashMap<>();
    private long lastObservedTick = Long.MIN_VALUE;

    public RecentCombatMemoryService(long retentionTicks) {
        if (retentionTicks <= 0) throw new IllegalArgumentException("Retention must be positive");
        this.retentionTicks = retentionTicks;
    }

    public boolean recordCommitted(CombatResult result, long serverTick) {
        if (result.context().attacker() == null || result.context().target() == null) return false;
        return recordCommitted(result.context().attacker().getUUID(), result.context().target().getUUID(),
                result.committedHealthDamage(), serverTick);
    }

    public boolean recordCommitted(
            UUID damagingEntity, UUID damagedEntity, double committedHealthDamage, long serverTick) {
        if (!Double.isFinite(committedHealthDamage) || committedHealthDamage <= 0.0) return false;
        record(damagingEntity, damagedEntity, serverTick);
        return true;
    }

    public void record(UUID damagingEntity, UUID damagedEntity, long serverTick) {
        prune(serverTick);
        history.computeIfAbsent(new Relationship(damagingEntity, damagedEntity), ignored -> new ArrayDeque<>())
                .addLast(serverTick);
    }

    public int count(UUID damagingEntity, UUID damagedEntity, long currentTick, long windowTicks) {
        if (windowTicks < 0) throw new IllegalArgumentException("Window must not be negative");
        prune(currentTick);
        var timestamps = history.get(new Relationship(damagingEntity, damagedEntity));
        if (timestamps == null) return 0;
        long earliest = currentTick - windowTicks;
        int count = 0;
        for (long timestamp : timestamps) if (timestamp >= earliest && timestamp <= currentTick) count++;
        return count;
    }

    public OptionalLong mostRecent(UUID damagingEntity, UUID damagedEntity, long currentTick, long windowTicks) {
        if (count(damagingEntity, damagedEntity, currentTick, windowTicks) == 0) return OptionalLong.empty();
        return OptionalLong.of(history.get(new Relationship(damagingEntity, damagedEntity)).getLast());
    }

    public void prune(long currentTick) {
        if (lastObservedTick != Long.MIN_VALUE && currentTick < lastObservedTick) history.clear();
        lastObservedTick = currentTick;
        long earliestRetained = currentTick - retentionTicks;
        var iterator = history.entrySet().iterator();
        while (iterator.hasNext()) {
            var timestamps = iterator.next().getValue();
            while (!timestamps.isEmpty() && timestamps.getFirst() < earliestRetained) timestamps.removeFirst();
            if (timestamps.isEmpty()) iterator.remove();
        }
    }

    public int relationshipCount() {
        return history.size();
    }

    public void clear() {
        history.clear();
        lastObservedTick = Long.MIN_VALUE;
    }

    private record Relationship(UUID damagingEntity, UUID damagedEntity) {}
}
