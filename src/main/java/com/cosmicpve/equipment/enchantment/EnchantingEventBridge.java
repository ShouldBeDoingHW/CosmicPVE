package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public final class EnchantingEventBridge {
    private final CustomEnchantCapacityService capacity = new CustomEnchantCapacityService();
    private final WhiteScrollProtectionService protection = new WhiteScrollProtectionService();

    public void onStacked(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() == ClickAction.PRIMARY
                && event.getCarriedItem().is(ModItems.HOLY_WHITE_SCROLL.get())
                && (HolyWhiteScrollService.eligible(event.getStackedOnItem())
                    || EquipmentInteractionPolicy.isPotentialEquipment(event.getStackedOnItem()))) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof ServerPlayer player) applyHolyScroll(event, player);
            return;
        }
        if (event.getClickAction() == ClickAction.PRIMARY
                && event.getCarriedItem().is(ModItems.COSMIC_DUST.get())
                && event.getStackedOnItem().is(ModItems.COSMIC_ENCHANTMENT_BOOK.get())) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof ServerPlayer player) applyDust(event, player);
            return;
        }
        if (event.getClickAction() != ClickAction.PRIMARY || !EquipmentInteractionPolicy.isPotentialEquipment(event.getStackedOnItem())) return;
        boolean book = event.getCarriedItem().is(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        boolean scroll = event.getCarriedItem().is(ModItems.WHITE_SCROLL.get());
        boolean transmog = event.getCarriedItem().is(ModItems.TRANSMOG_SCROLL.get());
        boolean orb = OrbType.fromStack(event.getCarriedItem()).isPresent();
        boolean higherOrb = event.getCarriedItem().getItem() instanceof HigherLoreOrbItem;
        boolean blackScroll = event.getCarriedItem().is(ModItems.BLACK_SCROLL.get());
        boolean enchantedBlackScroll = event.getCarriedItem().is(ModItems.ENCHANTED_BLACK_SCROLL.get());
        if (!book && !scroll && !transmog && !orb && !higherOrb && !blackScroll && !enchantedBlackScroll) return;
        event.setCanceled(true);
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (book) applyBook(event, player);
        else if (scroll) applyScroll(event, player);
        else if (transmog) applyTransmog(event, player);
        else if (higherOrb) applyHigherLoreOrb(event, player);
        else if (orb) applyOrb(event, player);
        else if (blackScroll) applyBlackScroll(event, player);
        else openEnchantedBlackScroll(event, player);
    }

    private void applyHolyScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var outcome = new HolyWhiteScrollService().apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (outcome == HolyWhiteScrollService.Outcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
        }
    }

    private void applyDust(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var result = new CosmicDustService().apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (result.outcome() == CosmicDustService.ApplicationOutcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
        }
    }

    private void applyBook(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var service = new CosmicBookApplicationService(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT),
                capacity, protection, () -> player.getRandom().nextInt(100) + 1,
                stored -> com.cosmicpve.upgrade.SafetyNetService.effectiveDestroyRate(stored,
                        new com.cosmicpve.upgrade.PlayerUpgradeService().tier(
                                player, com.cosmicpve.upgrade.PlayerUpgrade.SAFETY_NET)));
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(result.outcome()));
    }

    private void applyScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        if (event.getStackedOnItem() != event.getSlot().getItem()) return;
        if (!protection.apply(event.getSlot().getItem())) {
            return;
        }
        event.getCarriedItem().shrink(1);
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        com.cosmicpve.equipment.armor.ArmorCrystalFeedback.play(player,
                com.cosmicpve.equipment.armor.ArmorCrystalApplicationService.Outcome.SUCCESS);
    }

    private void applyTransmog(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var outcome = new TransmogApplicationService().apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(outcome));
    }

    private void applyOrb(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var service = new OrbApplicationService(capacity, protection, () -> player.getRandom().nextInt(100) + 1);
        var result = service.apply(event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        ItemApplicationFeedback.play(player, ItemApplicationFeedback.cueFor(result.outcome()));
    }

    private void applyHigherLoreOrb(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var outcome = new HigherLoreOrbApplicationService(capacity).apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (outcome == HigherLoreOrbApplicationService.Outcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
            return;
        }
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
            return;
        }
    }

    private void openEnchantedBlackScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        if (event.getStackedOnItem() != event.getSlot().getItem()) return;
        var data = event.getCarriedItem().get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        var candidates = new EnchantedBlackScrollExtractionService().candidates(event.getSlot().getItem());
        if (data == null || candidates.isEmpty()) {
            return;
        }
        ItemStack target = event.getSlot().getItem();
        ItemStack scrollStack = event.getCarriedItem();
        int rows = candidates.size() <= 8 ? 1 : 2;
        event.getSlot().set(ItemStack.EMPTY);
        event.getCarriedSlotAccess().set(ItemStack.EMPTY);
        var opened = player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inventory, ignored) -> new EnchantedBlackScrollMenu(id, inventory, rows, target, scrollStack),
                EnchantedBlackScrollMenu.TITLE), buffer -> buffer.writeVarInt(rows));
        if (opened.isEmpty()) new com.cosmicpve.reward.RewardDeliveryService().deliver(player,
                java.util.List.of(target, scrollStack));
    }
}
