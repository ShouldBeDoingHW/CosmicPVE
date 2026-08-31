package com.cosmicpve.economy.flashsale;

import net.neoforged.neoforge.common.NeoForge;

public final class FlashSaleBootstrap {
    private FlashSaleBootstrap() {}
    public static void register() {
        var bridge = new FlashSaleEventBridge();
        NeoForge.EVENT_BUS.addListener(bridge::onServerTick);
    }
}
