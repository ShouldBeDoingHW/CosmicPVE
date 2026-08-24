package com.cosmicpve.trial;

import com.cosmicpve.network.TrialTimerPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sends participant-only timer updates only when the authoritative displayed second changes. */
public final class TrialTimerDisplayService {
    private final Map<UUID, Integer> displayedSeconds = new HashMap<>();

    public void update(MinecraftServer server, TrialSession session) {
        int seconds = displayedSeconds(session.timerTicks());
        var active = new HashSet<>(session.participants());
        displayedSeconds.keySet().stream().filter(id -> !active.contains(id)).toList().forEach(id -> hide(server, id));
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && accept(id, seconds)) {
                PacketDistributor.sendToPlayer(player, new TrialTimerPayload(seconds));
            }
        }
    }

    public void show(ServerPlayer player, int timerTicks) {
        int seconds = displayedSeconds(timerTicks);
        displayedSeconds.put(player.getUUID(), seconds);
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(seconds));
    }

    public void hide(ServerPlayer player) {
        displayedSeconds.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
    }

    public void hideAll(MinecraftServer server) {
        java.util.List.copyOf(displayedSeconds.keySet()).forEach(id -> hide(server, id));
    }

    private void hide(MinecraftServer server, UUID id) {
        displayedSeconds.remove(id);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
    }

    public static int displayedSeconds(int ticks) { return ticks <= 0 ? 0 : (ticks + 19) / 20; }
    public static String formatTicks(int ticks) {
        int seconds = displayedSeconds(ticks);
        return seconds / 60 + ":" + String.format(java.util.Locale.ROOT, "%02d", seconds % 60);
    }
    boolean accept(UUID player, int seconds) {
        return !Integer.valueOf(seconds).equals(displayedSeconds.put(player, seconds));
    }
}
