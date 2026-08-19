package com.cosmicpve;

import com.cosmicpve.network.CosmicNetwork;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CosmicPVE.MOD_ID)
public final class CosmicPVE {
    public static final String MOD_ID = "cosmicpve";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CosmicPVE(IEventBus modBus) {
        ModDataComponents.register(modBus);
        ModAttachments.register(modBus);
        ModItems.register(modBus);
        modBus.addListener(CosmicNetwork::registerPayloads);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
