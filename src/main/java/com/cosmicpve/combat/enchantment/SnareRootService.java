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

    public void apply(LivingEntity target, long currentTick) {
        RootState existing = roots.get(target.getUUID());
        double ceilingY = existing == null ? target.getY() : Math.min(existing.ceilingY(), target.getY());
        roots.put(target.getUUID(), new RootState(target.getX(), target.getZ(), ceilingY,
                currentTick + DURATION_TICKS));
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
            state = new RootState(entity.getX(), entity.getZ(), entity.getY(), state.expiresAtTick());
            roots.put(entity.getUUID(), state);
        }
        double y = Math.min(entity.getY(), state.ceilingY());
        if (entity.getX() != state.x() || entity.getZ() != state.z() || entity.getY() > state.ceilingY()) {
            entity.setPos(state.x(), y, state.z());
        }
        if (y < state.ceilingY()) {
            roots.put(entity.getUUID(), new RootState(state.x(), state.z(), y, state.expiresAtTick()));
        }
        var motion = entity.getDeltaMovement();
        entity.setDeltaMovement(0.0, Math.min(0.0, motion.y), 0.0);
        entity.setSprinting(false);
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        entity.hurtMarked = true;
    }

    public void clear(UUID entityId) {
        roots.remove(entityId);
    }

    private record RootState(double x, double z, double ceilingY, long expiresAtTick) {}
}
