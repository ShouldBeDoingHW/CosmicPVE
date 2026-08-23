package com.cosmicpve.equipment.repair;

import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import com.cosmicpve.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class RepairScrollEventBridge {
    private final RepairScrollService service = new RepairScrollService();
    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.PRIMARY || !event.getCarriedItem().is(ModItems.REPAIR_SCROLL.get())
                || !event.getStackedOnItem().isDamageableItem()) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (result == RepairScrollService.Outcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
            player.displayClientMessage(Component.translatable("message.cosmicpve.repair_scroll.success"), true);
        } else {
            player.displayClientMessage(Component.translatable(result == RepairScrollService.Outcome.ALREADY_REPAIRED
                    ? "message.cosmicpve.repair_scroll.full" : "message.cosmicpve.repair_scroll.invalid"), true);
        }
    }
}
