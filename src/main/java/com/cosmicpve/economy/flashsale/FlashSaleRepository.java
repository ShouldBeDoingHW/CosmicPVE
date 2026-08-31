package com.cosmicpve.economy.flashsale;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class FlashSaleRepository {
    private static final SavedDataType<FlashSaleSavedData> TYPE = new SavedDataType<>(
            "cosmicpve_flash_sales", FlashSaleSavedData::new, FlashSaleSavedData.CODEC);

    public FlashSaleSavedData data(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }
    public void flush(MinecraftServer server) {
        server.getLevel(Level.OVERWORLD).getDataStorage().scheduleSave().join();
    }
}
