package com.cosmicpve.spacechest;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class SpaceChestEventBridge {
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SpaceChestSessionService.INSTANCE.recover(player);
    }
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SpaceChestSessionService.INSTANCE.recover(player);
    }
}
