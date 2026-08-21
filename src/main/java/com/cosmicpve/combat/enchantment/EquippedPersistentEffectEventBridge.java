package com.cosmicpve.combat.enchantment;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class EquippedPersistentEffectEventBridge {
    private final EquippedPersistentEffectService service;
    public EquippedPersistentEffectEventBridge(EquippedPersistentEffectService service) { this.service = service; }
    public void onPlayerTick(PlayerTickEvent.Post event) { service.tick(event.getEntity()); }
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living
                && !(living instanceof net.minecraft.world.entity.player.Player)) service.tick(living);
    }
}
