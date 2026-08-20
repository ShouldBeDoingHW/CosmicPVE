package com.cosmicpve.combat.cooldown;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

/** Central server-tick cooldown store. Callers never calculate or store their own expiry. */
public final class CooldownService {
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Identifier, CooldownEntry>> entries =
            new ConcurrentHashMap<>();

    public boolean isReady(UUID ownerId, Identifier key, long currentTick) {
        return remainingTicks(ownerId, key, currentTick) == 0;
    }

    public long remainingTicks(UUID ownerId, Identifier key, long currentTick) {
        var ownerEntries = entries.get(ownerId);
        if (ownerEntries == null) {
            return 0;
        }
        var entry = ownerEntries.get(key);
        if (entry == null) {
            return 0;
        }
        long remaining = Math.max(0L, entry.expiresAtTick() - currentTick);
        if (remaining == 0L) {
            ownerEntries.remove(key, entry);
            if (ownerEntries.isEmpty()) {
                entries.remove(ownerId, ownerEntries);
            }
        }
        return remaining;
    }

    public long start(
            UUID ownerId,
            Identifier key,
            long baseTicks,
            List<Double> durationMultipliers,
            long currentTick,
            CooldownScope scope,
            Optional<Identifier> scopeId) {
        long duration = effectiveDuration(baseTicks, durationMultipliers);
        set(ownerId, key, duration, currentTick, scope, scopeId);
        return duration;
    }

    /** Development and recovery seam for assigning an exact remaining duration. */
    public void set(
            UUID ownerId,
            Identifier key,
            long remainingTicks,
            long currentTick,
            CooldownScope scope,
            Optional<Identifier> scopeId) {
        if (remainingTicks < 0) {
            throw new IllegalArgumentException("Remaining cooldown ticks cannot be negative");
        }
        if (remainingTicks == 0) {
            clear(ownerId, key);
            return;
        }
        long expiry;
        try {
            expiry = Math.addExact(currentTick, remainingTicks);
        } catch (ArithmeticException exception) {
            expiry = Long.MAX_VALUE;
        }
        entries.computeIfAbsent(ownerId, ignored -> new ConcurrentHashMap<>())
                .put(key, new CooldownEntry(expiry, scope, scopeId));
    }

    public boolean clear(UUID ownerId, Identifier key) {
        var ownerEntries = entries.get(ownerId);
        if (ownerEntries == null) {
            return false;
        }
        boolean removed = ownerEntries.remove(key) != null;
        if (ownerEntries.isEmpty()) {
            entries.remove(ownerId, ownerEntries);
        }
        return removed;
    }

    public int clearAll(UUID ownerId) {
        var removed = entries.remove(ownerId);
        return removed == null ? 0 : removed.size();
    }

    public int clearTemporary(UUID ownerId) {
        return clearMatching(ownerId, entry -> entry.scope() != CooldownScope.PERSISTENT_PLAYER);
    }

    public int clearScope(UUID ownerId, CooldownScope scope, Optional<Identifier> scopeId) {
        return clearMatching(ownerId, entry -> entry.scope() == scope && entry.scopeId().equals(scopeId));
    }

    public List<Map.Entry<Identifier, CooldownEntry>> active(UUID ownerId, long currentTick) {
        var ownerEntries = entries.get(ownerId);
        if (ownerEntries == null) {
            return List.of();
        }
        var result = new ArrayList<Map.Entry<Identifier, CooldownEntry>>();
        ownerEntries.forEach((key, entry) -> {
            if (remainingTicks(ownerId, key, currentTick) > 0) {
                result.add(Map.entry(key, entry));
            }
        });
        result.sort(Comparator.comparing(entry -> entry.getKey().toString()));
        return List.copyOf(result);
    }

    /** Snapshot boundary for a later attachment/saved-data persistence adapter. */
    public Map<Identifier, CooldownEntry> snapshot(UUID ownerId, long currentTick) {
        var result = new java.util.LinkedHashMap<Identifier, CooldownEntry>();
        active(ownerId, currentTick).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return Map.copyOf(result);
    }

    public static long effectiveDuration(long baseTicks, List<Double> durationMultipliers) {
        if (baseTicks < 0) {
            throw new IllegalArgumentException("Base cooldown ticks cannot be negative");
        }
        double effective = baseTicks;
        for (double multiplier : List.copyOf(durationMultipliers)) {
            if (!Double.isFinite(multiplier) || multiplier < 0.0) {
                throw new IllegalArgumentException("Cooldown duration multipliers must be finite and non-negative");
            }
            effective *= multiplier;
        }
        effective = Math.ceil(effective);
        if (!Double.isFinite(effective) || effective >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return (long) effective;
    }

    private int clearMatching(UUID ownerId, java.util.function.Predicate<CooldownEntry> predicate) {
        var ownerEntries = entries.get(ownerId);
        if (ownerEntries == null) {
            return 0;
        }
        int before = ownerEntries.size();
        ownerEntries.entrySet().removeIf(entry -> predicate.test(entry.getValue()));
        if (ownerEntries.isEmpty()) {
            entries.remove(ownerId, ownerEntries);
        }
        return before - ownerEntries.size();
    }
}
