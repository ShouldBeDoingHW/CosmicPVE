package com.cosmicpve.conquest;

import net.neoforged.neoforge.common.NeoForge;

public final class ConquestBootstrap {
    private ConquestBootstrap() {}
    public static void register() {
        var bridge = new ConquestEventBridge(ConquestRuntime.events());
        NeoForge.EVENT_BUS.addListener(bridge::onServerTick);
        NeoForge.EVENT_BUS.addListener(bridge::onLeftClick);
        NeoForge.EVENT_BUS.addListener(bridge::onBreak);
        NeoForge.EVENT_BUS.addListener(bridge::onPlace);
        NeoForge.EVENT_BUS.addListener(bridge::onUseItem);
        NeoForge.EVENT_BUS.addListener(bridge::onExplosion);
        NeoForge.EVENT_BUS.addListener(bridge::onPiston);
        NeoForge.EVENT_BUS.addListener(bridge::onFluid);
        NeoForge.EVENT_BUS.addListener(bridge::onLivingDestroy);
        NeoForge.EVENT_BUS.addListener(bridge::onMobGrief);
    }
}
