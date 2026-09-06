package com.cosmicpve.adventure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AdventureGatewayBlock extends Block {
    public AdventureGatewayBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity,InsideBlockEffectApplier effects,boolean intersects) {
        if(intersects && entity instanceof ServerPlayer player)DenseWoodlandsBootstrap.SESSIONS.extract(player,pos);
    }
}
