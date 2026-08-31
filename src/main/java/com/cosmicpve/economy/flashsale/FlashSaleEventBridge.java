package com.cosmicpve.economy.flashsale;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class FlashSaleEventBridge {
    public void onServerTick(ServerTickEvent.Post event) { FlashSaleRuntime.service().tick(event.getServer()); }
}
