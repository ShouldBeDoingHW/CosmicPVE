package com.cosmicpve.trial.persistence;

import com.cosmicpve.trial.TrialSession;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class TrialSessionRepository {
    private static final SavedDataType<TrialSavedData> TYPE = new SavedDataType<>(
            "cosmicpve_trial_session", TrialSavedData::new, TrialSavedData.CODEC);

    private TrialSavedData data(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<TrialSession> active(MinecraftServer server) { return data(server).activeSession(); }

    public void publish(MinecraftServer server, TrialSession session) {
        data(server).setActiveSession(session);
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }

    public void publishVolatile(MinecraftServer server, TrialSession session) {
        data(server).setActiveSessionVolatile(session);
    }

    public void flush(MinecraftServer server) {
        data(server).markCheckpointDirty();
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }

    public void clear(MinecraftServer server) {
        data(server).clearActiveSession();
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }
}
