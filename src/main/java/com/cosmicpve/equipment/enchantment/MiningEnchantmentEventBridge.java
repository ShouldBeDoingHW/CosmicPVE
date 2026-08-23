package com.cosmicpve.equipment.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class MiningEnchantmentEventBridge {
    private final MiningEnchantmentService service = new MiningEnchantmentService();
    public void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof ServerPlayer) service.apply(event);
    }
}
