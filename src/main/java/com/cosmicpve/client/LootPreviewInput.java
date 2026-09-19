package com.cosmicpve.client;

import com.cosmicpve.network.LootPreviewPayload;
import com.cosmicpve.reward.preview.LootPreviewProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class LootPreviewInput {
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        var client = Minecraft.getInstance();
        if (!event.isAttack() || client.screen != null || client.player == null || client.hitResult == null
                || client.hitResult.getType() != HitResult.Type.MISS
                || !(client.player.getMainHandItem().getItem() instanceof LootPreviewProvider)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        ClientPacketDistributor.sendToServer(new LootPreviewPayload());
    }
}
