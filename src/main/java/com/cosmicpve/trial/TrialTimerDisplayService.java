package com.cosmicpve.trial;

import com.cosmicpve.network.TrialTimerPayload;
import com.cosmicpve.network.TrialOwnerPayload;
import com.cosmicpve.network.TrialPhasePayload;
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
    private final Map<UUID, String> displayedOwners = new HashMap<>();
    private final Map<UUID, TrialPhase> displayedPhases = new HashMap<>();

    public void update(MinecraftServer server, TrialSession session) {
        int seconds = displayedSeconds(session.timerTicks());
        var active = new HashSet<>(session.participants());
        displayedSeconds.keySet().stream().filter(id -> !active.contains(id)).toList().forEach(id -> hide(server, id));
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) showOwnerIfChanged(player, session.owner().header());
            if (player != null) showPhaseIfChanged(player, session.progress().phase());
            if (player != null && accept(id, seconds)) {
                PacketDistributor.sendToPlayer(player, new TrialTimerPayload(seconds));
            }
        }
    }

    public void show(ServerPlayer player, TrialSession session) {
        int seconds = displayedSeconds(session.timerTicks());
        displayedSeconds.put(player.getUUID(), seconds);
        showOwnerIfChanged(player, session.owner().header());
        showPhaseIfChanged(player, session.progress().phase());
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(seconds));
    }

    public void hide(ServerPlayer player) {
        displayedSeconds.remove(player.getUUID());
        displayedOwners.remove(player.getUUID());
        displayedPhases.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
        PacketDistributor.sendToPlayer(player, new TrialOwnerPayload(""));
        PacketDistributor.sendToPlayer(player, new TrialPhasePayload("", 0));
    }

    public void hideAll(MinecraftServer server) {
        java.util.List.copyOf(displayedSeconds.keySet()).forEach(id -> hide(server, id));
    }

    private void hide(MinecraftServer server, UUID id) {
        displayedSeconds.remove(id);
        displayedOwners.remove(id);
        displayedPhases.remove(id);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
            PacketDistributor.sendToPlayer(player, new TrialOwnerPayload(""));
            PacketDistributor.sendToPlayer(player, new TrialPhasePayload("", 0));
        }
    }

    public static int displayedSeconds(int ticks) { return ticks <= 0 ? 0 : (ticks + 19) / 20; }
    public static String formatTicks(int ticks) {
        int seconds = displayedSeconds(ticks);
        return seconds / 60 + ":" + String.format(java.util.Locale.ROOT, "%02d", seconds % 60);
    }
    boolean accept(UUID player, int seconds) {
        return !Integer.valueOf(seconds).equals(displayedSeconds.put(player, seconds));
    }

    boolean acceptOwner(UUID player, String heading) {
        return !heading.equals(displayedOwners.put(player, heading));
    }

    boolean acceptPhase(UUID player, TrialPhase phase) {
        return phase != displayedPhases.put(player, phase);
    }

    private void showOwnerIfChanged(ServerPlayer player, String heading) {
        if (acceptOwner(player.getUUID(), heading)) {
            PacketDistributor.sendToPlayer(player, new TrialOwnerPayload(heading));
        }
    }

    private void showPhaseIfChanged(ServerPlayer player, TrialPhase phase) {
        if (acceptPhase(player.getUUID(), phase)) {
            PacketDistributor.sendToPlayer(player, new TrialPhasePayload(phase.displayName(), phase.color()));
        }
    }
}
