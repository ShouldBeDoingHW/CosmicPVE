package com.cosmicpve.equipment.armor;

import com.cosmicpve.content.CosmicContent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class ArmorCrystalEventBridge {
    public void onChat(net.neoforged.neoforge.event.ServerChatEvent event) {
        if (ArmorCrystalConfirmationService.INSTANCE.chat(event.getPlayer(), event.getRawText())) event.setCanceled(true);
    }
    public void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) ArmorCrystalConfirmationService.INSTANCE.tick(player);
    }
    public void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ArmorCrystalConfirmationService.INSTANCE.cancel(player, false);
    }
    public void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ArmorCrystalConfirmationService.INSTANCE.cancel(player, false);
    }
    public void onStacked(ItemStackedOnOtherEvent event) {
        var decision = ArmorCrystalInteractionPolicy.decide(
                event.getCarriedItem(), event.getStackedOnItem(), event.getClickAction());
        if (decision == ArmorCrystalInteractionPolicy.Decision.VANILLA) return;
        event.setCanceled(true); // only genuine armor application gestures replace vanilla handling
        boolean logicalServer = event.getPlayer() instanceof ServerPlayer;
        if (logicalServer) {
            var player = (ServerPlayer) event.getPlayer();
            if (event.getStackedOnItem().has(com.cosmicpve.registry.ModDataComponents.ARMOR_SET_ID.get())) {
                ArmorCrystalConfirmationService.INSTANCE.begin(player, event.getCarriedItem(), event.getSlot());
                event.getCarriedSlotAccess().set(event.getCarriedItem());
                player.containerMenu.broadcastChanges();
                return;
            }
            ArmorCrystalConfirmationService.INSTANCE.cancel(player, false);
        }
        var result = ArmorCrystalInteractionPolicy.invokeAuthoritative(decision, logicalServer, () -> {
            var player = (ServerPlayer) event.getPlayer();
            var service = new ArmorCrystalApplicationService(
                    CosmicContent.repository(), () -> player.getRandom().nextInt(100) + 1);
            return service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        });
        if (result.isEmpty()) return;
        var player = (ServerPlayer) event.getPlayer();

        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        var outcome = result.orElseThrow();
        ArmorCrystalFeedback.play(player, outcome);
        String key = switch (outcome) {
            case SUCCESS -> "message.cosmicpve.armor_crystal.success";
            case FAILED_DESTROYED -> "message.cosmicpve.armor_crystal.failure";
            case FAILED_PROTECTED -> "message.cosmicpve.armor_crystal.protected";
            case ALREADY_SET -> "message.cosmicpve.armor_crystal.already_set";
            case INVALID_TARGET -> "message.cosmicpve.armor_crystal.invalid_target";
            case STALE_TARGET -> "message.cosmicpve.armor_crystal.stale";
            default -> "message.cosmicpve.armor_crystal.invalid";
        };
        player.displayClientMessage(Component.translatable(key), true);
    }
}
