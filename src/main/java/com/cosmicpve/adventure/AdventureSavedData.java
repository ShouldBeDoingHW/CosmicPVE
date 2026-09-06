package com.cosmicpve.adventure;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.*;

/** Same synchronous SavedData checkpoint convention as TrialSessionRepository. */
public final class AdventureSavedData extends SavedData {
    public static final Codec<AdventureSavedData> CODEC = AdventureSession.CODEC.listOf().fieldOf("sessions").codec()
            .xmap(AdventureSavedData::new,AdventureSavedData::all);
    private static final SavedDataType<AdventureSavedData> TYPE = new SavedDataType<>("cosmicpve_adventures",AdventureSavedData::new,CODEC);
    private final Map<UUID,AdventureSession> sessions = new HashMap<>();
    public AdventureSavedData() {}
    private AdventureSavedData(List<AdventureSession> values) { values.forEach(s -> sessions.put(s.owner(),s)); }
    public static AdventureSavedData get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }
    public List<AdventureSession> all() { return List.copyOf(sessions.values()); }
    public AdventureSession get(UUID owner) { return sessions.get(owner); }
    public void put(MinecraftServer server, AdventureSession session) { sessions.put(session.owner(),session); flush(server); }
    public void remove(MinecraftServer server, UUID owner) { sessions.remove(owner); flush(server); }
    private void flush(MinecraftServer server) { setDirty(); server.overworld().getDataStorage().scheduleSave().join(); }
}
