package com.cosmicpve.equipment.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class MiningEnchantmentEventBridge {
    private final MiningEnchantmentService service = new MiningEnchantmentService();
    public void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof ServerPlayer) service.apply(event);
    }
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) return;
        event.setNewSpeed(service.applyObsidianDestroyer(
                event.getEntity(), event.getNewSpeed(), event.getState(), event.getEntity().getMainHandItem()));
    }
}
