package com.cosmicpve.equipment.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Thin item-use adapter; ordinary block and item interactions retain priority. */
public final class SuperbreakerEventBridge {
    private final SuperbreakerService service = new SuperbreakerService();

    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (service.activate(player, event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
