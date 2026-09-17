package com.cosmicpve.equipment.accessory;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class BeltRuntimeEventBridge {
    private final BeltCombatService combat;
    public BeltRuntimeEventBridge(BeltCombatService combat) { this.combat = combat; }
    public void onPlayerTick(PlayerTickEvent.Post event) { combat.reconcile(event.getEntity()); }
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) combat.reset(player);
    }
}
