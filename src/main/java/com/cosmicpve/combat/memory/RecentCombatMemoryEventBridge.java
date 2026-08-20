package com.cosmicpve.combat.memory;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Periodic cleanup adapter; record/query semantics remain in the service. */
public final class RecentCombatMemoryEventBridge {
    private static final int CLEANUP_INTERVAL_TICKS = 200;
    private final RecentCombatMemoryService memory;

    public RecentCombatMemoryEventBridge(RecentCombatMemoryService memory) {
        this.memory = memory;
    }

    public void onServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();
        if (tick % CLEANUP_INTERVAL_TICKS == 0) memory.prune(tick);
    }
}
