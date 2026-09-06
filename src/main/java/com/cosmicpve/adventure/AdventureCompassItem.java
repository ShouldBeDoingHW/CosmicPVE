package com.cosmicpve.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;

public final class AdventureCompassItem extends CompassItem {
    public AdventureCompassItem(Properties properties) { super(properties); }
    @Override public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if(level.isClientSide())return net.minecraft.world.InteractionResult.SUCCESS;
        return player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && DenseWoodlandsBootstrap.SESSIONS.hint(serverPlayer,player.getItemInHand(hand))
                ? net.minecraft.world.InteractionResult.SUCCESS_SERVER : net.minecraft.world.InteractionResult.FAIL;
    }
    @Override public Component getName(ItemStack stack) { return Component.literal("Adventure Compass"); }
}
