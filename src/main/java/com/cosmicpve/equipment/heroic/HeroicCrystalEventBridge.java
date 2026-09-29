package com.cosmicpve.equipment.heroic;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.equipment.enchantment.ItemApplicationFeedback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class HeroicCrystalEventBridge {
    private final HeroicApplicationService service = new HeroicApplicationService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.PRIMARY || !event.getCarriedItem().is(ModItems.HEROIC_CRYSTAL.get())
                || HeroicApplicationService.kind(event.getStackedOnItem()).isEmpty()) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var outcome = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getStackedOnItem());
        if (outcome == HeroicApplicationService.Outcome.SUCCESS)
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
    }
}
