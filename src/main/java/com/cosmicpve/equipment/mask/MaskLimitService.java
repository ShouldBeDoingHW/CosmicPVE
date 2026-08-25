package com.cosmicpve.equipment.mask;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class MaskLimitService {
    private static final SavedDataType<MaskLimitSavedData> TYPE = new SavedDataType<>(
            "cosmicpve_mask_settings", MaskLimitSavedData::new, MaskLimitSavedData.CODEC);
    private MaskLimitSavedData data(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }
    public int get(MinecraftServer server) { return data(server).limit(); }
    public void set(MinecraftServer server, int limit) {
        data(server).setLimit(limit);
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }
}
