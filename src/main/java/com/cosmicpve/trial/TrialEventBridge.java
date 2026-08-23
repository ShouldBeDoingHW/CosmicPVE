package com.cosmicpve.trial;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class TrialEventBridge {
    public void onServerStarted(ServerStartedEvent event) { TrialRuntime.sessions().recoverInterrupted(event.getServer()); }
    public void onServerTick(ServerTickEvent.Post event) { TrialRuntime.sessions().tick(event.getServer()); }
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onLogin(player);
    }
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onDisconnect(player);
    }
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onRespawn(player);
    }
    public void onDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) TrialRuntime.sessions().onDeath(player);
    }
}
