package com.cosmicpve.reward.animation;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class LootAnimationEventBridge {
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SingleRewardAnimationService.INSTANCE.recover(player);
    }
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SingleRewardAnimationService.INSTANCE.recover(player);
    }
}
