package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
            player.displayClientMessage(Component.translatable("message.cosmicpve.holy_white_scroll.applied"), true);
        } else {
            String key = switch (outcome) {
                case REJECTED_UNPROTECTED -> "message.cosmicpve.holy_white_scroll.unprotected";
                case REJECTED_ALREADY_HOLY -> "message.cosmicpve.holy_white_scroll.already";
                default -> "message.cosmicpve.holy_white_scroll.invalid";
            };
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private void applyDust(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var result = new CosmicDustService().apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (result.outcome() == CosmicDustService.ApplicationOutcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
            player.displayClientMessage(Component.translatable("message.cosmicpve.dust.success",
                    result.consumed(), result.successAfter()), true);
        } else {
            String key = switch (result.outcome()) {
                case REJECTED_RARITY -> "message.cosmicpve.dust.rarity";
                case REJECTED_CAPPED -> "message.cosmicpve.dust.capped";
                default -> "message.cosmicpve.dust.invalid";
            };
            player.displayClientMessage(Component.translatable(key), true);
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
        String key = switch (result.outcome()) {
            case SUCCESS -> "message.cosmicpve.book.success";
            case FAILED_SURVIVED -> "message.cosmicpve.book.failed_survived";
            case FAILED_DESTROYED -> "message.cosmicpve.book.failed_destroyed";
            case FAILED_PROTECTED -> "message.cosmicpve.book.failed_protected";
            case REJECTED_CAPACITY -> "message.cosmicpve.book.capacity";
            case REJECTED_EXISTING_LEVEL -> "message.cosmicpve.book.existing";
            case REJECTED_HEROIC_PREREQUISITE -> "message.cosmicpve.book.heroic_prerequisite";
            case REJECTED_HEROIC_COUNTERPART -> "message.cosmicpve.book.heroic_counterpart";
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

    private void applyHigherLoreOrb(ItemStackedOnOtherEvent event, ServerPlayer player) {
        var outcome = new HigherLoreOrbApplicationService(capacity).apply(
                event.getCarriedItem(), event.getStackedOnItem(), event.getSlot().getItem());
        event.getCarriedSlotAccess().set(event.getCarriedItem());
        event.getSlot().set(event.getSlot().getItem());
        if (outcome == HigherLoreOrbApplicationService.Outcome.SUCCESS) {
            ItemApplicationFeedback.play(player, ItemApplicationFeedback.Cue.SUCCESS);
            player.displayClientMessage(Component.literal("Higher-lore capacity unlocked."), true);
            return;
        }
        String message = switch (outcome) {
            case REJECTED_TARGET -> "That orb cannot be applied to this item.";
            case REJECTED_CAPACITY -> "The previous lore capacity must be unlocked first.";
            default -> "The higher-lore orb could not be applied.";
        };
        player.displayClientMessage(Component.literal(message), true);
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

    private void openEnchantedBlackScroll(ItemStackedOnOtherEvent event, ServerPlayer player) {
        if (event.getStackedOnItem() != event.getSlot().getItem()) return;
        var data = event.getCarriedItem().get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        var candidates = new EnchantedBlackScrollExtractionService().candidates(event.getSlot().getItem());
        if (data == null || candidates.isEmpty()) {
            player.displayClientMessage(Component.translatable(data == null
                    ? "message.cosmicpve.enchanted_black_scroll.invalid"
                    : "message.cosmicpve.enchanted_black_scroll.no_eligible"), true);
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
