package com.cosmicpve.equipment.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Supported vanilla-freeze integration; future effects query the same immunity resolver. */
public final class ArmorSetEventBridge {
    private final ArmorSetImmunityResolver immunities;
    private final ArmorSetResolver sets;
    private final ArmorSetAttributeService attributes;
    public ArmorSetEventBridge(ArmorSetImmunityResolver immunities, ArmorSetResolver sets,
            ArmorSetAttributeService attributes) {
        this.immunities = immunities; this.sets = sets; this.attributes = attributes;
    }

    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) tick(player);
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)
                && !living.level().isClientSide()) tick(living);
    }

    public void onKnockback(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent event) {
        sets.resolve(event.getEntity()).filter(d -> d.id().equals(ArmorSetIds.ANCIENT)).ifPresent(ignored ->
                event.setStrength((float) (event.getStrength() * AncientArmorSetBehavior.knockback(
                        event.getEntity().getHealth(), event.getEntity().getMaxHealth()))));
    }

    private void tick(LivingEntity entity) { protectFromFreeze(entity); attributes.reconcile(entity); }

    private void protectFromFreeze(LivingEntity entity) {
        if (immunities.isImmune(entity, ArmorSetIds.FREEZE)) entity.clearFreeze();
    }
}
