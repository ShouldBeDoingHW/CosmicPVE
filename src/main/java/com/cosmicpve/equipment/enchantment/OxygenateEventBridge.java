package com.cosmicpve.equipment.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** BlockDropsEvent is downstream of an accepted block break and carries the authoritative tool. */
public final class OxygenateEventBridge {
    private final OxygenateService service;
    public OxygenateEventBridge(OxygenateService service) { this.service = service; }
    public void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof ServerPlayer player) service.applyCompletedBreak(player, event.getTool());
    }
}
