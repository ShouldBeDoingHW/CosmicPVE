package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.conquest.ConquestChestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CosmicPVE.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConquestChestBlockEntity>> CONQUEST_CHEST =
            TYPES.register("conquest_chest", () -> new BlockEntityType<>(
                    ConquestChestBlockEntity::new, ModBlocks.CONQUEST_CHEST.get()));
    private ModBlockEntities() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
