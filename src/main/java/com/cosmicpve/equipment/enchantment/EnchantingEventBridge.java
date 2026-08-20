package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class EnchantingEventBridge {
    private final CustomEnchantCapacityService capacity = new CustomEnchantCapacityService();
    private final WhiteScrollProtectionService protection = new WhiteScrollProtectionService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.PRIMARY || !EquipmentInteractionPolicy.isPotentialEquipment(event.getStackedOnItem())) return;
        boolean book = event.getCarriedItem().is(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        boolean scroll = event.getCarriedItem().is(ModItems.WHITE_SCROLL.get());
        boolean transmog = event.getCarriedItem().is(ModItems.TRANSMOG_SCROLL.get());
        boolean orb = OrbType.fromStack(event.getCarriedItem()).isPresent();
        boolean blackScroll = event.getCarriedItem().is(ModItems.BLACK_SCROLL.get());
        if (!book && !scroll && !transmog && !orb && !blackScroll) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (book) applyBook(event, player);
        else if (scroll) applyScroll(event, player);
        else if (transmog) applyTransmog(event, player);
        else if (orb) applyOrb(event, player);
        else applyBlackScroll(event, player);
    }

    private void applyBook(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var service = new CosmicBookApplicationService(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT),
                capacity, protection, () -> player.getRandom().nextInt(100) + 1);
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(result.outcome()));
        String key = switch (result.outcome()) {
            case SUCCESS -> "message.cosmicpve.book.success";
            case FAILED_SURVIVED -> "message.cosmicpve.book.failed_survived";
            case FAILED_DESTROYED -> "message.cosmicpve.book.failed_destroyed";
            case FAILED_PROTECTED -> "message.cosmicpve.book.failed_protected";
            case REJECTED_CAPACITY -> "message.cosmicpve.book.capacity";
            case REJECTED_EXISTING_LEVEL -> "message.cosmicpve.book.existing";
            case REJECTED_TARGET -> "message.cosmicpve.book.incompatible";
            default -> "message.cosmicpve.book.invalid";
        };
        player.displayClientMessage(Component.translatable(key), true);
    }

    private void applyScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        if (event.getStackedOnItem() != event.getSlot().getItem()) return;
        if (!protection.apply(event.getSlot().getItem())) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.white_scroll.already"), true);
            return;
        }
        event.getCarriedItem().shrink(1);
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        com.cosmicpve.equipment.armor.ArmorCrystalFeedback.play(player,
                com.cosmicpve.equipment.armor.ArmorCrystalApplicationService.Outcome.SUCCESS);
        player.displayClientMessage(Component.translatable("message.cosmicpve.white_scroll.applied"), true);
    }

    private void applyTransmog(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var outcome = new TransmogApplicationService().apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(outcome));
        if (outcome == TransmogApplicationService.Outcome.SUCCESS) {
            player.displayClientMessage(Component.translatable("message.cosmicpve.transmog.applied"), true);
        } else {
            String key = outcome == TransmogApplicationService.Outcome.REJECTED_ALREADY_APPLIED
                    ? "message.cosmicpve.transmog.already" : "message.cosmicpve.transmog.invalid";
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private void applyOrb(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var service = new OrbApplicationService(capacity, protection, () -> player.getRandom().nextInt(100) + 1);
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(result.outcome()));
        String key = switch (result.outcome()) {
            case SUCCESS -> "message.cosmicpve.orb.success";
            case FAILED_SURVIVED -> "message.cosmicpve.orb.failed_survived";
            case FAILED_PROTECTED -> "message.cosmicpve.orb.failed_protected";
            case FAILED_DESTROYED -> "message.cosmicpve.orb.failed_destroyed";
            case REJECTED_TARGET -> "message.cosmicpve.orb.target";
            case REJECTED_MAX_CAPACITY -> "message.cosmicpve.orb.maximum";
            default -> "message.cosmicpve.orb.invalid";
        };
        player.displayClientMessage(Component.translatable(key), true);
    }

    private void applyBlackScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var service = new BlackScrollExtractionService(
                player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT),
                bound -> player.getRandom().nextInt(bound),
                () -> player.getRandom().nextInt(100) + 1);
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        if (result.succeeded()) {
            // The non-stackable consumed scroll leaves the cursor empty, so the generated book can occupy it safely.
            event.getCarriedSlotAccess().set(BlackScrollCursorOutput.afterApplication(
                    event.getCarriedItem(), result));
            event.getSlot().set(event.getSlot().getItem());
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
            player.displayClientMessage(Component.translatable("message.cosmicpve.black_scroll.success",
                    Component.translatable("enchantment." + result.enchantmentId().getNamespace() + "."
                            + result.enchantmentId().getPath()), result.level()), true);
            return;
        }
        String key = result.outcome() == BlackScrollExtractionResult.Outcome.REJECTED_NO_ELIGIBLE_ENCHANTMENTS
                ? "message.cosmicpve.black_scroll.no_eligible" : "message.cosmicpve.black_scroll.invalid";
        player.displayClientMessage(Component.translatable(key), true);
    }
}
