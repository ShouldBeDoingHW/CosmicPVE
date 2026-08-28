package com.cosmicpve.conquest;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import com.cosmicpve.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A real, unusually durable chest block whose identity is the event boundary. */
public final class ConquestChestBlock extends ChestBlock {
    public static final float DESTROY_TIME = 80.0F;
    public static final float EXPLOSION_RESISTANCE = 1_200.0F;
    private static final float IRON_PICKAXE_PROGRESS = 6.0F / DESTROY_TIME / 30.0F;
    private static final float NETHERITE_EFFICIENCY_FIVE_PROGRESS = 35.0F / DESTROY_TIME / 30.0F;
    private static final float ENDGAME_TARGET_PROGRESS = 1.0F / 200.0F;
    private static final float ACCELERATED_PROGRESS_SCALE =
            (ENDGAME_TARGET_PROGRESS - IRON_PICKAXE_PROGRESS)
                    / (NETHERITE_EFFICIENCY_FIVE_PROGRESS - IRON_PICKAXE_PROGRESS);

    public ConquestChestBlock(Properties properties) {
        super(ModBlockEntities.CONQUEST_CHEST::get, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
    }

    @Override public boolean chestCanConnectTo(BlockState state) { return false; }

    /**
     * Vanilla Efficiency V adds 26 mining speed, so one raw hardness cannot satisfy both the intended
     * ordinary-iron and endgame timings. Preserve vanilla tool/effect ordering while compressing only
     * acceleration above the unenchanted iron baseline for this event block.
     */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return calibrateDestroyProgress(super.getDestroyProgress(state, player, level, pos));
    }

    static float calibrateDestroyProgress(float vanillaProgress) {
        if (vanillaProgress <= IRON_PICKAXE_PROGRESS) return vanillaProgress;
        return IRON_PICKAXE_PROGRESS
                + (vanillaProgress - IRON_PICKAXE_PROGRESS) * ACCELERATED_PROGRESS_SCALE;
    }

    @Override
    public ConquestChestBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConquestChestBlockEntity(pos, state);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            ConquestRuntime.events().interact(serverPlayer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            ConquestRuntime.events().complete(serverPlayer, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }
}
