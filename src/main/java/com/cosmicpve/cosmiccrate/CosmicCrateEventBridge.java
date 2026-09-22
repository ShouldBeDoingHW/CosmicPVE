package com.cosmicpve.cosmiccrate;

import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class CosmicCrateEventBridge {
    private final CosmicCrateCombinationService service = new CosmicCrateCombinationService();
    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.PRIMARY
                || !(event.getCarriedItem().getItem() instanceof CosmicCrateHalfItem first)
                || !(event.getStackedOnItem().getItem() instanceof CosmicCrateHalfItem second)
                || first.season() != second.season() || first.side() == second.side()) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var outcome = service.combine(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        if (outcome != CosmicCrateCombinationService.Outcome.SUCCESS) return;
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(SeasonalCosmicCrates.complete(first.season()));
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
    }
}
