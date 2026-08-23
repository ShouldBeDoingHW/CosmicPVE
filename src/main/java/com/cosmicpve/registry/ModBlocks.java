package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.trial.portal.TrialGatewayBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CosmicPVE.MOD_ID);
    public static final DeferredBlock<TrialGatewayBlock> TRIAL_GATEWAY = BLOCKS.registerBlock(
            "trial_gateway", TrialGatewayBlock::new,
            properties -> properties.noCollision().noOcclusion().strength(-1.0F, 3_600_000.0F)
                    .lightLevel(state -> 11));
    private ModBlocks() {}
    public static void register(IEventBus modBus) { BLOCKS.register(modBus); }
}
