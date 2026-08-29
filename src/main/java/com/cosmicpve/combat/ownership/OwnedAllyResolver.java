package com.cosmicpve.combat.ownership;

import java.util.Optional;
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
}
