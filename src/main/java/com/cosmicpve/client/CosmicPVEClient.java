package com.cosmicpve.client;

import com.cosmicpve.CosmicPVE;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.client.entity.SpacePirateVariant1Renderer;
import com.cosmicpve.client.entity.SpacePirateVariant2Renderer;
import com.cosmicpve.client.entity.UndeadCorpseRenderer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CosmicPVE.MOD_ID, dist = Dist.CLIENT)
public final class CosmicPVEClient {
    public CosmicPVEClient(IEventBus modBus) {
        modBus.addListener(ArmorSetClientExtensions::register);
        modBus.addListener(CosmicPVEClient::registerItemTints);
        modBus.addListener(CosmicPVEClient::registerEntityRenderers);
        modBus.addListener(CosmicPVEClient::registerMenuScreens);
        modBus.addListener(TrialClientPresentation::registerPayloadHandlers);
        modBus.addListener(TrialClientPresentation::registerGuiLayers);
        modBus.addListener(MaskClientPresentation::register);
        modBus.addListener(ScrollableItemTooltip::registerTooltipComponents);
        var scrollingTooltips = new ScrollableItemTooltip();
        NeoForge.EVENT_BUS.addListener(scrollingTooltips::onGather);
        NeoForge.EVENT_BUS.addListener(scrollingTooltips::onRender);
        NeoForge.EVENT_BUS.addListener(scrollingTooltips::onMouseScrolled);
        NeoForge.EVENT_BUS.addListener(scrollingTooltips::onScreenClosing);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(com.cosmicpve.registry.ModMenus.SPACE_CHEST.get(), SpaceChestScreen::new);
        event.register(com.cosmicpve.registry.ModMenus.TRIAL_DECISION.get(), TrialDecisionScreen::new);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPACE_PIRATE_VARIANT_1.get(), SpacePirateVariant1Renderer::new);
        event.registerEntityRenderer(ModEntities.SPACE_PIRATE_VARIANT_2.get(), SpacePirateVariant2Renderer::new);
        event.registerEntityRenderer(ModEntities.UNDEAD_CORPSE.get(), UndeadCorpseRenderer::new);
    }

    private static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(CosmicPVE.id("armor_set"), ArmorSetItemTintSource.MAP_CODEC);
        event.register(CosmicPVE.id("cosmic_book"), CosmicBookItemTintSource.MAP_CODEC);
    }
}
