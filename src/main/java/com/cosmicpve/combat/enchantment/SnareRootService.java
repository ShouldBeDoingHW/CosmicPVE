package com.cosmicpve.combat.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/** Short-lived server-only root state. Translation and self-propelled ascent are denied; gravity remains active. */
public final class SnareRootService {
    public static final int DURATION_TICKS = 25;
    private static final double TELEPORT_DISPLACEMENT = 2.0;
    private final Map<UUID, RootState> roots = new HashMap<>();
    private final Map<UUID, TrapAnchor> trapAnchors = new HashMap<>();
    private final java.util.Set<UUID> teleporting = new java.util.HashSet<>();

    public void apply(LivingEntity target, long currentTick) {
        apply(target, currentTick, DURATION_TICKS, 0.0);
    }

    public void apply(LivingEntity target, long currentTick, int durationTicks, double meleeVulnerability) {
        RootState existing = roots.get(target.getUUID());
        double ceilingY = existing == null ? target.getY() : Math.min(existing.ceilingY(), target.getY());
        roots.put(target.getUUID(), new RootState(target.getX(), target.getZ(), ceilingY,
                currentTick + durationTicks, Math.max(existing == null ? 0.0 : existing.meleeVulnerability(), meleeVulnerability)));
    }

    public boolean isRooted(UUID entityId, long currentTick) {
        RootState state = roots.get(entityId);
        if (state == null) return false;
        if (currentTick >= state.expiresAtTick()) {
            roots.remove(entityId);
            return false;
        }
        return true;
    }

    public long remainingTicks(UUID entityId, long currentTick) {
        RootState state = roots.get(entityId);
        return state == null ? 0L : Math.max(0L, state.expiresAtTick() - currentTick);
    }

    public double meleeVulnerability(UUID entityId, long currentTick) {
        RootState state = roots.get(entityId);
        return state != null && currentTick < state.expiresAtTick() ? state.meleeVulnerability() : 0.0;
    }

    public void tick(LivingEntity entity, long currentTick) {
        RootState state = roots.get(entity.getUUID());
        if (state == null) return;
        if (currentTick >= state.expiresAtTick() || entity.isRemoved() || entity.isDeadOrDying()) {
            roots.remove(entity.getUUID());
            return;
        }

        if (Math.abs(entity.getX() - state.x()) > TELEPORT_DISPLACEMENT
                || Math.abs(entity.getZ() - state.z()) > TELEPORT_DISPLACEMENT
                || entity.getY() - state.ceilingY() > TELEPORT_DISPLACEMENT) {
            state = new RootState(entity.getX(), entity.getZ(), entity.getY(), state.expiresAtTick(), state.meleeVulnerability());
            roots.put(entity.getUUID(), state);
        }
        double y = restrain(entity, state.x(), state.z(), state.ceilingY());
        if (y < state.ceilingY()) {
            roots.put(entity.getUUID(), new RootState(state.x(), state.z(), y, state.expiresAtTick(), state.meleeVulnerability()));
        }
    }

    /** Trap's lifetime is owned by the negative CombatStack; this stores only the positional anchor. */
    public void applyTrap(LivingEntity entity) {
        trapAnchors.putIfAbsent(entity.getUUID(), new TrapAnchor(entity.getX(), entity.getZ(), entity.getY()));
        stopMotion(entity);
    }

    public void tickTrap(LivingEntity entity, boolean active) {
        if (!active || entity.isRemoved() || entity.isDeadOrDying()) {
            trapAnchors.remove(entity.getUUID());
            teleporting.remove(entity.getUUID());
            return;
        }
        TrapAnchor anchor = trapAnchors.computeIfAbsent(entity.getUUID(), ignored ->
                new TrapAnchor(entity.getX(), entity.getZ(), entity.getY()));
        if (teleporting.remove(entity.getUUID())
                || Math.abs(entity.getX() - anchor.x()) > TELEPORT_DISPLACEMENT
                || Math.abs(entity.getZ() - anchor.z()) > TELEPORT_DISPLACEMENT
                || entity.getY() - anchor.ceilingY() > TELEPORT_DISPLACEMENT) {
            anchor = new TrapAnchor(entity.getX(), entity.getZ(), entity.getY());
            trapAnchors.put(entity.getUUID(), anchor);
        }
        double y = restrain(entity, anchor.x(), anchor.z(), anchor.ceilingY());
        if (y < anchor.ceilingY()) trapAnchors.put(entity.getUUID(), new TrapAnchor(anchor.x(), anchor.z(), y));
    }

    public boolean hasTrapAnchor(UUID entityId) { return trapAnchors.containsKey(entityId); }

    /** Event-mediated authoritative teleports, including short operator moves, reset Trap's anchor next tick. */
    public void markTeleport(UUID entityId) { if (trapAnchors.containsKey(entityId)) teleporting.add(entityId); }

    private static double restrain(LivingEntity entity, double x, double z, double ceilingY) {
        double y = Math.min(entity.getY(), ceilingY);
        if (entity.getX() != x || entity.getZ() != z || entity.getY() > ceilingY)
            entity.setPos(x, y, z);
        stopMotion(entity);
        return y;
    }

    private static void stopMotion(LivingEntity entity) {
        var motion = entity.getDeltaMovement();
        entity.setDeltaMovement(0.0, Math.min(0.0, motion.y), 0.0);
        entity.setSprinting(false);
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        entity.hurtMarked = true;
    }

    public void clear(UUID entityId) {
        roots.remove(entityId);
    }

    private record RootState(double x, double z, double ceilingY, long expiresAtTick, double meleeVulnerability) {}
    private record TrapAnchor(double x, double z, double ceilingY) {}
}
