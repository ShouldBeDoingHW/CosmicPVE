package com.cosmicpve.trial;

import com.cosmicpve.network.TrialCelebrationPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;

/** Non-transactional, harmless cash-out celebration; disconnect simply cancels pending launches. */
public final class TrialCelebrationService {
    public static final int INTERVAL_TICKS = 10;
    private static final int[] COLORS = {0xFFAA00, 0xFFFF55, 0x55FF55, 0xA3FFF5, 0xAA00AA};
    private final Map<UUID, Schedule> schedules = new HashMap<>();

    public boolean schedule(ServerPlayer player, int completedRooms, long currentTick) {
        if (completedRooms <= 0 || schedules.containsKey(player.getUUID())) return false;
        schedules.put(player.getUUID(), new Schedule(completedRooms, 0, currentTick));
        return true;
    }
    public void cancel(UUID playerId) { schedules.remove(playerId); }

    public void tick(MinecraftServer server) {
        var iterator = schedules.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID id = entry.getKey();
            Schedule schedule = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) { iterator.remove(); continue; }
            if (server.getTickCount() < schedule.nextTick()) continue;
            launch(player, schedule.launched());
            int launched = schedule.launched() + 1;
            if (launched >= schedule.total()) iterator.remove();
            else entry.setValue(new Schedule(schedule.total(), launched, schedule.nextTick() + INTERVAL_TICKS));
        }
    }

    private static void launch(ServerPlayer player, int index) {
        double x = player.getX(), y = player.getY() + 2.2, z = player.getZ();
        PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel) player.level(), null,
                x, y, z, 64.0, new TrialCelebrationPayload(x, y, z, COLORS[index % COLORS.length]));
        player.level().playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    static java.util.List<Integer> launchOffsets(int completedRooms) {
        return java.util.stream.IntStream.range(0, Math.max(0, completedRooms)).map(i -> i * INTERVAL_TICKS).boxed().toList();
    }

    private record Schedule(int total, int launched, long nextTick) {}
}
