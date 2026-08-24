package com.cosmicpve.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CosmicNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private CosmicNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(TrialTimerPayload.TYPE, TrialTimerPayload.STREAM_CODEC);
        registrar.playToClient(TrialOwnerPayload.TYPE, TrialOwnerPayload.STREAM_CODEC);
        registrar.playToClient(TrialCelebrationPayload.TYPE, TrialCelebrationPayload.STREAM_CODEC);
    }
}
