package com.cosmicpve.trial.trinket;

import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class TrialTrinketEventBridge {
    private final TrialTrinketApplicationService service = new TrialTrinketApplicationService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.PRIMARY
                || !event.getStackedOnItem().is(ModItems.TRIAL_PORTAL.get())
                || !event.getCarriedItem().has(ModDataComponents.TRIAL_TRINKET.get())) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var outcome = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (outcome == TrialTrinketApplicationService.Outcome.SUCCESS)
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
    }
}
