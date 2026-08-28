package com.cosmicpve.conquest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class ConquestRepository {
    private static final SavedDataType<ConquestSavedData> TYPE = new SavedDataType<>(
            "cosmicpve_conquest_events", ConquestSavedData::new, ConquestSavedData.CODEC);

    private ConquestSavedData data(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }
    public long lastScheduledDay(MinecraftServer server) { return data(server).lastScheduledDay(); }
    public void setLastScheduledDay(MinecraftServer server, long day) { data(server).setLastScheduledDay(day); }
    public List<ConquestEvent> active(MinecraftServer server) {
        return data(server).events().stream().filter(event -> event.state() == ConquestEventState.ACTIVE).toList();
    }
    public List<ConquestEvent> all(MinecraftServer server) { return data(server).events(); }
    public Optional<ConquestEvent> find(MinecraftServer server, UUID id) {
        return data(server).events().stream().filter(event -> event.id().equals(id)).findFirst();
    }
    public Optional<ConquestEvent> at(MinecraftServer server, net.minecraft.core.BlockPos position) {
        return active(server).stream().filter(event -> event.chestPosition().equals(position)).findFirst();
    }
    public void publish(MinecraftServer server, ConquestEvent event) { data(server).upsert(event); }
    public void remove(MinecraftServer server, UUID id) { data(server).remove(id); }
    public void flush(MinecraftServer server) {
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }
}
