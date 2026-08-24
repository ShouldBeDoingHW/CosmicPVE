package com.cosmicpve.trial;

import com.cosmicpve.instance.protection.InstanceProtectionEventBridge;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;

public final class TrialBootstrap {
    private TrialBootstrap() {}
    public static void register() {
        var trials = new TrialEventBridge();
        NeoForge.EVENT_BUS.addListener(trials::onServerStarted);
        NeoForge.EVENT_BUS.addListener(trials::onServerTick);
        NeoForge.EVENT_BUS.addListener(trials::onLogin);
        NeoForge.EVENT_BUS.addListener(trials::onLogout);
        NeoForge.EVENT_BUS.addListener(trials::onRespawn);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, trials::onDeath);
        NeoForge.EVENT_BUS.addListener(trials::onDrops);
        NeoForge.EVENT_BUS.addListener(trials::onExperience);
        NeoForge.EVENT_BUS.addListener(trials::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(trials::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(trials::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(trials::onEntityJoin);
        var protection = new InstanceProtectionEventBridge(TrialRuntime.protection());
        NeoForge.EVENT_BUS.addListener(protection::onBreak);
        NeoForge.EVENT_BUS.addListener(protection::onPlace);
        NeoForge.EVENT_BUS.addListener(protection::onUseItem);
        NeoForge.EVENT_BUS.addListener(protection::onExplosion);
        NeoForge.EVENT_BUS.addListener(protection::onPiston);
        NeoForge.EVENT_BUS.addListener(protection::onFluidPlace);
        NeoForge.EVENT_BUS.addListener(protection::onLivingDestroy);
        NeoForge.EVENT_BUS.addListener(protection::onMobGrief);
    }
}
