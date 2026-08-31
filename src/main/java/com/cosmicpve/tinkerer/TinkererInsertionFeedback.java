package com.cosmicpve.tinkerer;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

/** Player-local feedback emitted only when an item actually enters a Tinkerer input slot. */
final class TinkererInsertionFeedback {
    private TinkererInsertionFeedback() {}

    static Holder<SoundEvent> sound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }

    static boolean shouldPlay(int beforeCount, int afterCount, boolean fromPlayerInventory, ClickType clickType) {
        return fromPlayerInventory && afterCount > beforeCount
                && (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE);
    }

    static boolean shouldPlay(ItemStack slotBefore, ItemStack slotAfter, ItemStack carriedBefore,
            boolean fromPlayerInventory, ClickType clickType) {
        if (!fromPlayerInventory || clickType != ClickType.PICKUP || carriedBefore.isEmpty()
                || slotAfter.isEmpty() || !ItemStack.isSameItemSameComponents(slotAfter, carriedBefore)) return false;
        return !ItemStack.isSameItemSameComponents(slotBefore, slotAfter)
                || slotAfter.getCount() > slotBefore.getCount();
    }

    static ClientboundSoundPacket packet(ServerPlayer player) {
        return new ClientboundSoundPacket(sound(), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, player.getRandom().nextLong());
    }

    static void play(ServerPlayer player) { player.connection.send(packet(player)); }
}
