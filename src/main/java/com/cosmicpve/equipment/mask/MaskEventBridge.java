package com.cosmicpve.equipment.mask;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import com.cosmicpve.reward.RewardDeliveryService;

public final class MaskEventBridge {
    private final MaskApplicationService masks = new MaskApplicationService();
    private final MaskSplicerService splicer = new MaskSplicerService();
    private final RewardDeliveryService rewards = new RewardDeliveryService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.PRIMARY && event.getCarriedItem().is(ModItems.MASK_SPLICER.get())
                && event.getStackedOnItem().is(ModItems.MASK.get())) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var result = splicer.split(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem());
            event.getSlot().set(event.getSlot().getItem());
            event.getCarriedSlotAccess().set(event.getCarriedItem());
            if (result.outcome() == MaskSplicerService.Outcome.SUCCESS) rewards.deliver(player, result.outputs());
            return;
        }
        if (event.getClickAction() == ClickAction.PRIMARY && event.getCarriedItem().is(ModItems.MASK.get())) {
            var equippable = event.getStackedOnItem().get(DataComponents.EQUIPPABLE);
            if (equippable == null || equippable.slot() != EquipmentSlot.HEAD) return;
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var outcome = masks.apply(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem());
            event.getCarriedSlotAccess().set(event.getCarriedItem());
            event.getSlot().set(event.getSlot().getItem());
            if (outcome == MaskApplicationService.ApplyOutcome.SUCCESS)
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, true);
            return;
        }
        if (event.getClickAction() == ClickAction.SECONDARY && event.getCarriedItem().isEmpty()
                && event.getStackedOnItem().has(ModDataComponents.MASK_LOADOUT.get())) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var result = masks.remove(event.getStackedOnItem(), event.getSlot().getItem(), true);
            if (result.outcome() == MaskApplicationService.RemoveOutcome.SUCCESS) {
                event.getSlot().set(event.getSlot().getItem());
                event.getCarriedSlotAccess().set(result.returnedMask());
                com.cosmicpve.equipment.skin.WeaponSkinFeedback.play(player, false);
            }
        }
    }
}
