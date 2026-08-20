package com.cosmicpve.equipment.skin;

import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class WeaponSkinEventBridge {
    private final WeaponSkinApplicationService service = new WeaponSkinApplicationService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.PRIMARY
                && event.getCarriedItem().is(ModItems.WEAPON_SKIN.get())
                && CustomEnchantCapacityService.isWeapon(event.getStackedOnItem())) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var outcome = service.apply(event.getCarriedItem(), event.getStackedOnItem(),
                    event.getCarriedItem(), event.getSlot().getItem());
            event.getCarriedSlotAccess().set(event.getCarriedItem());
            event.getSlot().set(event.getSlot().getItem());
            if (outcome == WeaponSkinApplicationService.ApplyOutcome.SUCCESS) WeaponSkinFeedback.play(player, true);
            player.displayClientMessage(Component.translatable(switch (outcome) {
                case SUCCESS -> "message.cosmicpve.skin.applied";
                case REJECTED_ALREADY_SKINNED -> "message.cosmicpve.skin.already";
                case REJECTED_TARGET -> "message.cosmicpve.skin.incompatible";
                default -> "message.cosmicpve.skin.invalid";
            }), true);
            return;
        }

        // Empty-cursor secondary click is the safe inventory interpretation of "right-click the skinned item".
        if (event.getClickAction() == ClickAction.SECONDARY && event.getCarriedItem().isEmpty()
                && event.getStackedOnItem().has(ModDataComponents.WEAPON_SKIN.get())) {
            event.setCanceled(true);
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            var result = service.remove(event.getStackedOnItem(), event.getSlot().getItem(), true);
            if (result.outcome() == WeaponSkinApplicationService.RemoveOutcome.SUCCESS) {
                event.getSlot().set(event.getSlot().getItem());
                event.getCarriedSlotAccess().set(result.returnedSkin());
                WeaponSkinFeedback.play(player, false);
                player.displayClientMessage(Component.translatable("message.cosmicpve.skin.removed"), true);
            }
        }
    }
}
