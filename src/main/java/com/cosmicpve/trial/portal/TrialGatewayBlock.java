package com.cosmicpve.trial.portal;

import com.cosmicpve.trial.TrialRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class TrialGatewayBlock extends Block {
    private static final java.util.Map<java.util.UUID, Integer> NEXT_REJECTION_MESSAGE = new java.util.HashMap<>();
    public TrialGatewayBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean intersects) {
        if (!level.isClientSide() && intersects && entity instanceof ServerPlayer player) {
            var result = TrialRuntime.sessions().join(player, pos);
            int now = player.level().getServer().getTickCount();
            if (!result.success() && now >= NEXT_REJECTION_MESSAGE.getOrDefault(player.getUUID(), 0)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(result.message()), true);
                NEXT_REJECTION_MESSAGE.put(player.getUUID(), now + 20);
            }
        }
    }
}
