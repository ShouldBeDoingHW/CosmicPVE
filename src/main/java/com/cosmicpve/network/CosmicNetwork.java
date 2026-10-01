package com.cosmicpve.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CosmicNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private CosmicNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(LootPreviewPayload.TYPE, LootPreviewPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player
                    && player.isAlive() && player.containerMenu == player.inventoryMenu
                    && LootPreviewPayload.leftClickAir(player)) {
                com.cosmicpve.reward.preview.LootPreviewMenu.openHeld(player);
            }
        });
        registrar.playToClient(TrialTimerPayload.TYPE, TrialTimerPayload.STREAM_CODEC);
        registrar.playToClient(TrialOwnerPayload.TYPE, TrialOwnerPayload.STREAM_CODEC);
        registrar.playToClient(TrialPhasePayload.TYPE, TrialPhasePayload.STREAM_CODEC);
        registrar.playToClient(TrialRoomPayload.TYPE, TrialRoomPayload.STREAM_CODEC);
        registrar.playToClient(TrialCelebrationPayload.TYPE, TrialCelebrationPayload.STREAM_CODEC);
        registrar.playToClient(HighlightPayload.TYPE, HighlightPayload.STREAM_CODEC);
    }
}
