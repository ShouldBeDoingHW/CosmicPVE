package com.cosmicpve.equipment.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Supported vanilla-freeze integration; future effects query the same immunity resolver. */
public final class ArmorSetEventBridge {
    private final ArmorSetImmunityResolver immunities;
    public ArmorSetEventBridge(ArmorSetImmunityResolver immunities) { this.immunities = immunities; }

    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) protectFromFreeze(player);
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)
                && !living.level().isClientSide()) protectFromFreeze(living);
    }

    private void protectFromFreeze(LivingEntity entity) {
        if (immunities.isImmune(entity, ArmorSetIds.FREEZE)) entity.clearFreeze();
    }
}
