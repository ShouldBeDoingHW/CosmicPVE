package com.cosmicpve.combat.ownership;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

/** Shared ownership boundary for tameables and Cosmic summoned allies. */
public final class OwnedAllyResolver {
    private OwnedAllyResolver() {}

    public static Optional<LivingEntity> owner(LivingEntity entity) {
        return entity instanceof OwnableEntity ownable ? Optional.ofNullable(ownable.getOwner()) : Optional.empty();
    }

    public static Optional<UUID> ownerId(Entity entity) {
        if (!(entity instanceof OwnableEntity ownable) || ownable.getOwnerReference() == null) return Optional.empty();
        return Optional.of(ownable.getOwnerReference().getUUID());
    }

    public static boolean allied(Entity owned, Entity other) {
        var owner = ownerId(owned);
        if (owner.isEmpty()) return false;
        return owner.get().equals(other.getUUID()) || ownerId(other).filter(owner.get()::equals).isPresent();
    }

    /** All currently loaded, living non-player allies with an authoritative owner reference. */
    public static List<LivingEntity> livingAllies(LivingEntity owner) {
        var server = owner.level().getServer();
        if (server == null) return List.of();
        var result = new ArrayList<LivingEntity>();
        for (var level : server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living
                        && !(living instanceof net.minecraft.world.entity.player.Player)
                        && living.isAlive() && !living.isRemoved()
                        && ownerId(living).filter(owner.getUUID()::equals).isPresent()) {
                    result.add(living);
                }
            }
        }
        return List.copyOf(result);
    }
}
