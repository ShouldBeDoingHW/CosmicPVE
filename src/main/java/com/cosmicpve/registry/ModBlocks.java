package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.trial.portal.TrialGatewayBlock;
import com.cosmicpve.conquest.ConquestChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CosmicPVE.MOD_ID);
    public static final DeferredBlock<TrialGatewayBlock> TRIAL_GATEWAY = BLOCKS.registerBlock(
            "trial_gateway", TrialGatewayBlock::new,
            properties -> properties.noCollision().noOcclusion().strength(-1.0F, 3_600_000.0F)
                    .lightLevel(state -> 11));
    public static final DeferredBlock<ConquestChestBlock> CONQUEST_CHEST = BLOCKS.registerBlock(
            "conquest_chest", ConquestChestBlock::new,
            properties -> properties.ofFullCopy(Blocks.CHEST)
                    .requiresCorrectToolForDrops()
                    .strength(ConquestChestBlock.DESTROY_TIME, ConquestChestBlock.EXPLOSION_RESISTANCE)
                    .noLootTable());
    public static final DeferredBlock<com.cosmicpve.adventure.AdventureGatewayBlock> ADVENTURE_GATEWAY = BLOCKS.registerBlock(
            "adventure_gateway", com.cosmicpve.adventure.AdventureGatewayBlock::new,
            properties -> properties.noCollision().noOcclusion().strength(-1F,3600000F).noLootTable().lightLevel(s -> 11));
    private ModBlocks() {}
    public static void register(IEventBus modBus) { BLOCKS.register(modBus); }
}
