package com.cosmicpve.equipment.mask;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

public final class MaskAnvilEventBridge {
    /** Vanilla AnvilMenu refuses result pickup at literal cost zero; this is refunded in Post. */
    public static final int PICKUP_GATE_COST = 1;
    private final MaskAnvilService anvils = new MaskAnvilService();
    private final MaskLimitService limits = new MaskLimitService();

    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!event.getLeft().is(ModItems.MASK.get()) || !event.getRight().is(ModItems.MASK.get())) return;
        int limit = event.getPlayer().level().isClientSide()
                ? MaskLimitSavedData.MAX_LIMIT
                : limits.get(((net.minecraft.server.level.ServerLevel) event.getPlayer().level()).getServer());
        var result = anvils.combine(event.getLeft(), event.getRight(), limit, CosmicContent.repository());
        event.setOutput(result.outcome() == MaskAnvilService.Outcome.SUCCESS ? result.output() : ItemStack.EMPTY);
        event.setXpCost(PICKUP_GATE_COST);
        event.setMaterialCost(1);
    }

    public void onAnvilCraft(net.neoforged.neoforge.event.entity.player.AnvilCraftEvent.Post event) {
        if (event.getEntity().level().isClientSide() || event.getEntity().hasInfiniteMaterials()
                || !event.getLeft().is(ModItems.MASK.get()) || !event.getRight().is(ModItems.MASK.get())
                || !event.getOutput().is(ModItems.MASK.get())) return;
        var output = event.getOutput().get(com.cosmicpve.registry.ModDataComponents.MASK_ITEM.get());
        if (output != null && output.valid() && output.maskIds().size() >= 2)
            event.getEntity().giveExperienceLevels(PICKUP_GATE_COST);
    }
}
