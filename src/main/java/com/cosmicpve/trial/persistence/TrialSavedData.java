package com.cosmicpve.trial.persistence;

import com.cosmicpve.trial.TrialSession;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.level.saveddata.SavedData;

public final class TrialSavedData extends SavedData {
    public static final Codec<TrialSavedData> CODEC = TrialSession.CODEC.optionalFieldOf("active_session")
            .codec().xmap(TrialSavedData::new, TrialSavedData::activeSession);
    private Optional<TrialSession> activeSession;

    public TrialSavedData() { this(Optional.empty()); }
    private TrialSavedData(Optional<TrialSession> activeSession) { this.activeSession = activeSession; }
    public Optional<TrialSession> activeSession() { return activeSession; }
    public void setActiveSession(TrialSession session) { activeSession = Optional.of(session); setDirty(); }
    public void clearActiveSession() { activeSession = Optional.empty(); setDirty(); }
}
