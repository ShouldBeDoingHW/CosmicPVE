package com.cosmicpve.trial;

import com.cosmicpve.network.TrialTimerPayload;
import com.cosmicpve.network.TrialOwnerPayload;
import com.cosmicpve.network.TrialPhasePayload;
import com.cosmicpve.network.TrialRoomPayload;
import com.cosmicpve.content.CosmicContent;
import java.util.HashMap;
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
    private final Map<UUID, RoomDisplay> displayedRooms = new HashMap<>();
    private TrialLifecycleState cachedRoomState;
    private net.minecraft.resources.Identifier cachedRoomId;
    private int cachedRoomOrdinal = -1;
    private RoomDisplay cachedRoom = RoomDisplay.DECISION_BOX;

    public void update(MinecraftServer server, TrialSession session) {
        int seconds = displayedSeconds(session.timerTicks());
        java.util.ArrayList<UUID> stale = null;
        for (UUID id : displayedSeconds.keySet()) if (!session.participants().contains(id)) {
            if (stale == null) stale = new java.util.ArrayList<>();
            stale.add(id);
        }
        if (stale != null) for (UUID id : stale) hide(server, id);
        String owner = session.owner().header();
        TrialPhase phase = session.progress().phase();
        RoomDisplay room = cachedRoom(session);
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) showOwnerIfChanged(player, owner);
            if (player != null) showPhaseIfChanged(player, phase);
            if (player != null) showRoomIfChanged(player, room);
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
        showRoomIfChanged(player, roomDisplay(session));
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(seconds));
    }

    public void hide(ServerPlayer player) {
        displayedSeconds.remove(player.getUUID());
        displayedOwners.remove(player.getUUID());
        displayedPhases.remove(player.getUUID());
        displayedRooms.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
        PacketDistributor.sendToPlayer(player, new TrialOwnerPayload(""));
        PacketDistributor.sendToPlayer(player, new TrialPhasePayload("", 0));
        PacketDistributor.sendToPlayer(player, new TrialRoomPayload(0, ""));
    }

    public void hideAll(MinecraftServer server) {
        java.util.List.copyOf(displayedSeconds.keySet()).forEach(id -> hide(server, id));
        cachedRoomState = null; cachedRoomId = null; cachedRoomOrdinal = -1; cachedRoom = RoomDisplay.DECISION_BOX;
    }

    private void hide(MinecraftServer server, UUID id) {
        displayedSeconds.remove(id);
        displayedOwners.remove(id);
        displayedPhases.remove(id);
        displayedRooms.remove(id);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new TrialTimerPayload(-1));
            PacketDistributor.sendToPlayer(player, new TrialOwnerPayload(""));
            PacketDistributor.sendToPlayer(player, new TrialPhasePayload("", 0));
            PacketDistributor.sendToPlayer(player, new TrialRoomPayload(0, ""));
        }
    }

    public static int displayedSeconds(int ticks) { return ticks <= 0 ? 0 : (ticks + 19) / 20; }
    public static String formatTicks(int ticks) {
        int seconds = displayedSeconds(ticks);
        return formatSeconds(seconds);
    }
    public static String formatSeconds(int seconds) {
        int safe = Math.max(0, seconds);
        return safe / 60 + "m " + String.format(java.util.Locale.ROOT, "%02d", safe % 60) + "s";
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

    boolean acceptRoom(UUID player, RoomDisplay room) { return !room.equals(displayedRooms.put(player, room)); }

    public static RoomDisplay roomDisplay(TrialSession session) {
        return roomDisplay(session, id -> CosmicContent.repository().requireTrialRoom(id).displayName());
    }

    private RoomDisplay cachedRoom(TrialSession session) {
        boolean inRoom = session.state() == TrialLifecycleState.ROOM_INTRO
                || session.state() == TrialLifecycleState.ROOM_ACTIVE;
        net.minecraft.resources.Identifier roomId = inRoom ? session.currentRoom().orElse(null) : null;
        int ordinal = inRoom ? session.progress().completedRooms() + 1 : 0;
        if (cachedRoomState != session.state() || !java.util.Objects.equals(cachedRoomId, roomId)
                || cachedRoomOrdinal != ordinal) {
            cachedRoomState = session.state(); cachedRoomId = roomId; cachedRoomOrdinal = ordinal;
            cachedRoom = roomDisplay(session);
        }
        return cachedRoom;
    }

    static RoomDisplay roomDisplay(TrialSession session,
                                   java.util.function.Function<net.minecraft.resources.Identifier, String> names) {
        if (session.state() == TrialLifecycleState.ROOM_INTRO || session.state() == TrialLifecycleState.ROOM_ACTIVE) {
            return session.currentRoom().map(id -> new RoomDisplay(session.progress().completedRooms() + 1,
                    names.apply(id))).orElse(RoomDisplay.DECISION_BOX);
        }
        return RoomDisplay.DECISION_BOX;
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

    private void showRoomIfChanged(ServerPlayer player, RoomDisplay room) {
        if (acceptRoom(player.getUUID(), room)) {
            PacketDistributor.sendToPlayer(player, new TrialRoomPayload(room.ordinal(), room.displayName()));
        }
    }

    public record RoomDisplay(int ordinal, String displayName) {
        public static final RoomDisplay DECISION_BOX = new RoomDisplay(0, "Decision Box");
        public RoomDisplay {
            if (ordinal < 0) throw new IllegalArgumentException("Room ordinal cannot be negative");
            displayName = displayName == null ? "" : displayName;
        }
    }
}
