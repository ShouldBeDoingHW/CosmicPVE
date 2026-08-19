package com.cosmicpve.content;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.reload.CosmicContentReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class CosmicContent {
    private static final CosmicContentRepository REPOSITORY = new CosmicContentRepository();

    private CosmicContent() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CosmicContent::addServerReloadListener);
    }

    public static CosmicContentRepository repository() {
        return REPOSITORY;
    }

    private static void addServerReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(
                CosmicPVE.id("content_definitions"),
                new CosmicContentReloadListener(REPOSITORY));
    }
}
