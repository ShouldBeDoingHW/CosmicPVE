package com.cosmicpve.equipment.accessory;

import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class AccessoryEventBridge {
    private final AccessoryApplicationService accessories = new AccessoryApplicationService();
    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.PRIMARY && event.getCarriedItem().is(ModItems.AMULET_SOCKET.get())) {
            if (!AccessoryApplicationService.isChestplate(event.getStackedOnItem())) return;
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var outcome = accessories.socket(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem(), () -> player.getRandom().nextInt(100) + 1);
            event.getCarriedSlotAccess().set(event.getCarriedItem()); event.getSlot().set(event.getSlot().getItem());
            if (outcome == AccessoryApplicationService.SocketOutcome.SUCCESS) ItemApplicationFeedback.play(player,
                    ItemApplicationFeedback.Cue.SUCCESS);
            if (outcome == AccessoryApplicationService.SocketOutcome.FAILED) ItemApplicationFeedback.play(player,
                    ItemApplicationFeedback.Cue.FAILED_SURVIVED);
            player.displayClientMessage(Component.translatable("message.cosmicpve.amulet_socket."
                    + outcome.name().toLowerCase()), true);
            return;
        }
        var carriedAccessory = event.getCarriedItem().get(ModDataComponents.ACCESSORY_ITEM.get());
        if (event.getClickAction() == ClickAction.PRIMARY && carriedAccessory != null
                && carriedAccessory.slot() == AccessorySlot.AMULET) {
            if (!AccessoryApplicationService.isChestplate(event.getStackedOnItem())) return;
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var outcome = accessories.attach(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem());
            event.getCarriedSlotAccess().set(event.getCarriedItem()); event.getSlot().set(event.getSlot().getItem());
            if (outcome == AccessoryApplicationService.AttachOutcome.SUCCESS)
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, true);
            player.displayClientMessage(Component.translatable("message.cosmicpve.amulet." + outcome.name().toLowerCase()), true);
            return;
        }
        var loadout = event.getStackedOnItem().get(ModDataComponents.ACCESSORY_LOADOUT.get());
        if (event.getClickAction() == ClickAction.SECONDARY && event.getCarriedItem().isEmpty()
                && loadout != null && loadout.attached(AccessorySlot.AMULET).isPresent()) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var result = accessories.remove(event.getStackedOnItem(), event.getSlot().getItem(), true);
            if (result.outcome() == AccessoryApplicationService.RemoveOutcome.SUCCESS) {
                event.getSlot().set(event.getSlot().getItem()); event.getCarriedSlotAccess().set(result.returnedAmulet());
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, false);
            }
            player.displayClientMessage(Component.translatable("message.cosmicpve.amulet.remove_"
                    + result.outcome().name().toLowerCase()), true);
        }
    }
}
