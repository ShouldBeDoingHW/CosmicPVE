package com.cosmicpve.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CosmicNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private CosmicNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Establish the versioned registration point. Feature-owned payloads are deliberately deferred.
        event.registrar(PROTOCOL_VERSION);
    }
}
