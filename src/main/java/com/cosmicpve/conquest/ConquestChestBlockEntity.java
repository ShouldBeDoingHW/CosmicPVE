package com.cosmicpve.conquest;

import com.cosmicpve.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ConquestChestBlockEntity extends ChestBlockEntity {
    public ConquestChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONQUEST_CHEST.get(), pos, state);
    }
}
