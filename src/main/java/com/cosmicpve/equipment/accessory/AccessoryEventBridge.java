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
        if (event.getClickAction() == ClickAction.PRIMARY && (event.getCarriedItem().is(ModItems.AMULET_SOCKET.get())
                || event.getCarriedItem().is(ModItems.BELT_SOCKET.get())
                || event.getCarriedItem().is(ModItems.OMNI_SOCKET.get()))) {
            boolean eligible = event.getCarriedItem().is(ModItems.AMULET_SOCKET.get())
                    ? AccessoryApplicationService.isChestplate(event.getStackedOnItem())
                    : event.getCarriedItem().is(ModItems.BELT_SOCKET.get())
                            ? AccessoryApplicationService.isLeggings(event.getStackedOnItem())
                            : AccessoryApplicationService.isChestplate(event.getStackedOnItem())
                                    || AccessoryApplicationService.isLeggings(event.getStackedOnItem());
            if (!eligible) return;
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            String kind = event.getCarriedItem().is(ModItems.AMULET_SOCKET.get()) ? "amulet_socket"
                    : event.getCarriedItem().is(ModItems.BELT_SOCKET.get()) ? "belt_socket" : "omni_socket";
            var outcome = accessories.socket(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem(), () -> player.getRandom().nextInt(100) + 1);
            event.getCarriedSlotAccess().set(event.getCarriedItem()); event.getSlot().set(event.getSlot().getItem());
            if (outcome == AccessoryApplicationService.SocketOutcome.SUCCESS) ItemApplicationFeedback.play(player,
                    ItemApplicationFeedback.Cue.SUCCESS);
            if (outcome == AccessoryApplicationService.SocketOutcome.FAILED) ItemApplicationFeedback.play(player,
                    ItemApplicationFeedback.Cue.FAILED_SURVIVED);
            player.displayClientMessage(Component.translatable("message.cosmicpve." + kind + "."
                    + outcome.name().toLowerCase()), true);
            return;
        }
        var carriedAccessory = event.getCarriedItem().get(ModDataComponents.ACCESSORY_ITEM.get());
        if (event.getClickAction() == ClickAction.PRIMARY && carriedAccessory != null
                && (carriedAccessory.slot() == AccessorySlot.AMULET || carriedAccessory.slot() == AccessorySlot.BELT)) {
            if (carriedAccessory.slot() == AccessorySlot.AMULET
                    ? !AccessoryApplicationService.isChestplate(event.getStackedOnItem())
                    : !AccessoryApplicationService.isLeggings(event.getStackedOnItem())) return;
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var outcome = accessories.attach(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem());
            event.getCarriedSlotAccess().set(event.getCarriedItem()); event.getSlot().set(event.getSlot().getItem());
            if (outcome == AccessoryApplicationService.AttachOutcome.SUCCESS)
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, true);
            String kind = carriedAccessory.slot() == AccessorySlot.AMULET ? "amulet" : "belt";
            player.displayClientMessage(Component.translatable("message.cosmicpve." + kind + "." + outcome.name().toLowerCase()), true);
            return;
        }
        var loadout = event.getStackedOnItem().get(ModDataComponents.ACCESSORY_LOADOUT.get());
        if (event.getClickAction() == ClickAction.SECONDARY && event.getCarriedItem().isEmpty()
                && loadout != null && (loadout.attached(AccessorySlot.AMULET).isPresent()
                        || loadout.attached(AccessorySlot.BELT).isPresent())) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var result = accessories.remove(event.getStackedOnItem(), event.getSlot().getItem(), true);
            if (result.outcome() == AccessoryApplicationService.RemoveOutcome.SUCCESS) {
                event.getSlot().set(event.getSlot().getItem()); event.getCarriedSlotAccess().set(result.returnedAmulet());
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, false);
            }
            String kind = AccessoryApplicationService.isLeggings(event.getStackedOnItem()) ? "belt" : "amulet";
            player.displayClientMessage(Component.translatable("message.cosmicpve." + kind + ".remove_"
                    + result.outcome().name().toLowerCase()), true);
        }
    }
}
