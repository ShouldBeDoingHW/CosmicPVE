package com.cosmicpve.adventure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Adventure-safe pots: no deposits; an empty main hand recovers already stored items. */
public final class WoodlandsPotInteractions {
    private WoodlandsPotInteractions() {}
    public static boolean protects(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                                   boolean creative, boolean spectator) {
        return dimension.equals(DenseWoodlandsSessionService.DIMENSION) && !creative && !spectator;
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (!protects(event.getLevel().dimension(), player.isCreative(), player.isSpectator())
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof DecoratedPotBlockEntity pot)) return;
        // Cancel on both logical sides, and consume the gesture to prevent an off-hand deposit.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer serverPlayer) || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (player.getMainHandItem().isEmpty()) recover(serverPlayer, pot);
        else player.displayClientMessage(Component.literal(
                "Woodlands pots cannot store items. Right-click with an empty hand to recover stored items."), true);
    }
    public static int recover(ServerPlayer player, DecoratedPotBlockEntity pot) {
        ItemStack stored = pot.getTheItem();
        if (stored.isEmpty()) return 0;
        int count = stored.getCount();
        // Clear before delivery: a repeated/off-hand click must not award the same contents twice.
        pot.setTheItem(ItemStack.EMPTY);
        pot.setChanged();
        var state = pot.getBlockState();
        player.level().sendBlockUpdated(pot.getBlockPos(), state, state, 3);
        player.getInventory().placeItemBackInInventory(stored);
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.literal("Recovered " + count + " item(s) from the pot."), true);
        return count;
    }
}
