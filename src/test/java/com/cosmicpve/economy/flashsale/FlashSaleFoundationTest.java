package com.cosmicpve.economy.flashsale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import org.junit.jupiter.api.Test;

class FlashSaleFoundationTest {
    @Test void catalogPreservesTwentyCanonicalRowsAndExplicitDeferredDependencies() {
        assertEquals(20, FlashSaleCatalog.CANONICAL_ROWS.size());
        assertEquals(19, FlashSaleCatalog.productionRows().size());
        assertFalse(FlashSaleCatalog.find("abandoned_spaceship_portal").orElseThrow().productionSelectable());
        assertEquals("memory_chest", FlashSaleCatalog.MEMORY_CHEST.id());
        assertTrue(FlashSaleCatalog.CANONICAL_ROWS.stream().noneMatch(row -> row.id().equals("memory_chest")));
    }

    @Test void exactCanonicalPricesAndDuplicateFamiliesRemainSeparateRows() {
        var portalOne = FlashSaleCatalog.find("trial_portal_1").orElseThrow();
        var portalTwo = FlashSaleCatalog.find("trial_portal_2").orElseThrow();
        assertEquals(1, portalOne.quantity()); assertEquals(2, portalTwo.quantity());
        assertEquals(20_000_000L, portalOne.lowPrice()); assertEquals(87_500_000L, portalTwo.highPrice());
        assertEquals(2, FlashSaleCatalog.CANONICAL_ROWS.stream().filter(row -> row.displayName().getString().equals("Repair Scroll")).count());
        assertEquals(120_000_000L, FlashSaleCatalog.find("cosmic_enchantment_table").orElseThrow().highPrice());
    }

    @Test void everyProductionRowAndPriceTierIsReachableByDeterministicBoundary() {
        var rows = FlashSaleCatalog.productionRows();
        for (int index = 0; index < rows.size(); index++) {
            int selected = index; assertEquals(rows.get(index), FlashSaleSchedule.select(rows, bound -> selected));
        }
        for (int index = 0; index < 3; index++) {
            int selected = index; assertEquals(FlashSalePriceTier.values()[index], FlashSaleSchedule.selectTier(bound -> selected));
        }
    }

    @Test void scheduleUsesInclusiveStartRangeAndExactReminderAndCloseBoundaries() {
        assertEquals(54_000L, FlashSaleSchedule.nextInterval(bound -> 0));
        assertEquals(90_000L, FlashSaleSchedule.nextInterval(bound -> bound - 1));
        var active = new FlashSaleActive("trial_portal_1", FlashSalePriceTier.LOW, 20_000_000L,
                1_000L, 7_000L, false, List.of());
        assertFalse(FlashSaleSchedule.reminderDue(active, 5_799L)); assertTrue(FlashSaleSchedule.reminderDue(active, 5_800L));
        assertFalse(FlashSaleSchedule.expired(active, 6_999L)); assertTrue(FlashSaleSchedule.expired(active, 7_000L));
        assertFalse(FlashSaleSchedule.reminderDue(active.withReminderSent(), 5_900L));
    }

    @Test void activeSaleCodecRoundTripsPurchasersAndExactSchedule() {
        UUID buyer = UUID.randomUUID();
        var active = new FlashSaleActive("repair_scroll_5", FlashSalePriceTier.MEDIUM, 40_000_000L,
                100L, 6_100L, true, List.of(buyer));
        var encoded = FlashSaleActive.CODEC.encodeStart(JsonOps.INSTANCE, active).getOrThrow();
        var decoded = FlashSaleActive.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(encoded.toString())).getOrThrow();
        assertEquals(active, decoded); assertTrue(decoded.purchased(buyer));
    }

    @Test void savedDataCodecPersistsActiveSalePurchasersAndNextStart() {
        UUID buyer = UUID.randomUUID();
        var active = new FlashSaleActive("mystery_simple_spawner", FlashSalePriceTier.LOW, 25_000_000L,
                200L, 6_200L, false, List.of(buyer));
        var data = new FlashSaleSavedData();
        data.setActive(Optional.of(active)); data.setNextStartTick(55_555L);
        var encoded = FlashSaleSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var decoded = FlashSaleSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(Optional.of(active), decoded.active()); assertEquals(55_555L, decoded.nextStartTick());
    }

    @Test void purchaseConstructsBeforeDebitAndMarksBeforeDeliveryExactlyOnce() {
        var calls = new ArrayList<String>();
        var outcome = FlashSalePurchaseTransaction.execute(true, false, 1_000L, 500L,
                () -> { calls.add("construct"); return Optional.of(List.of(new ItemStack(Items.APPLE))); },
                amount -> { calls.add("debit"); return true; }, () -> calls.add("mark"), rewards -> calls.add("deliver"));
        assertEquals(FlashSalePurchaseTransaction.Outcome.SUCCESS, outcome);
        assertEquals(List.of("construct", "debit", "mark", "deliver"), calls);
    }

    @Test void rejectionNeverMarksOrDeliversAndInsufficientFundsRemainEligible() {
        var calls = new ArrayList<String>();
        assertEquals(FlashSalePurchaseTransaction.Outcome.ALREADY_PURCHASED,
                FlashSalePurchaseTransaction.execute(true, true, 1_000L, 500L,
                        Optional::<List<ItemStack>>empty, amount -> true, () -> calls.add("mark"), value -> calls.add("deliver")));
        assertEquals(FlashSalePurchaseTransaction.Outcome.INSUFFICIENT_FUNDS,
                FlashSalePurchaseTransaction.execute(true, false, 499L, 500L,
                        () -> Optional.of(List.of(new ItemStack(Items.APPLE))), amount -> true,
                        () -> calls.add("mark"), value -> calls.add("deliver")));
        assertTrue(calls.isEmpty());
    }

    @Test void startAlertTargetsEveryConnectedRecipientExactlyOnceWithDragonGrowl() {
        var recipients = List.of("one", "two", "three");
        var alerts = new AtomicInteger();
        FlashSaleStartAlert.forEach(recipients, ignored -> alerts.incrementAndGet());
        assertEquals(recipients.size(), alerts.get());
        assertEquals(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, FlashSaleStartAlert.sound());
    }

    @Test void reminderAlertTargetsEveryConnectedRecipientExactlyOnceWithBeacon() {
        var recipients = List.of("one", "two", "three");
        var alerts = new AtomicInteger();
        FlashSaleReminderAlert.forEach(recipients, ignored -> alerts.incrementAndGet());
        assertEquals(recipients.size(), alerts.get());
        assertEquals(net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, FlashSaleReminderAlert.sound());
    }

    @Test void initialAndReminderOfferNamesKeepPresentationAndOpenCurrentPreview() {
        var service = new FlashSaleService();
        var entry = FlashSaleCatalog.find("heroic_crystal").orElseThrow();
        var sale = new FlashSaleActive(entry.id(), FlashSalePriceTier.LOW, entry.lowPrice(), 100L, 6_100L, false, List.of());
        for (var message : List.of(service.startMessage(entry, sale), service.reminder(entry, sale))) {
            var clickable = findClickable(message).orElseThrow();
            assertEquals("Heroic Crystal", clickable.getString());
            assertEquals(com.cosmicpve.equipment.heroic.HeroicCrystalItem.NAME_COLOR,
                    clickable.getStyle().getColor().getValue());
            assertTrue(clickable.getStyle().isUnderlined());
            assertEquals("/flashsale preview", ((ClickEvent.RunCommand) clickable.getStyle().getClickEvent()).command());
            assertEquals("Click to preview this item",
                    ((HoverEvent.ShowText) clickable.getStyle().getHoverEvent()).value().getString());
        }
    }

    @Test void representativeOfferIsStableAndPreviewQuantityNeverChangesCanonicalReward() {
        var service = new FlashSaleService();
        var entry = FlashSaleCatalog.find("armor_orb_100").orElseThrow();
        var sale = new FlashSaleActive(entry.id(), FlashSalePriceTier.LOW, entry.lowPrice(), 222L, 6_222L, false, List.of());
        var first = service.resolveOffer(entry, sale).orElseThrow().getFirst();
        var second = service.resolveOffer(entry, sale).orElseThrow().getFirst();
        assertEquals(first.getComponents(), second.getComponents());
        var portal = FlashSaleCatalog.find("trial_portal_2").orElseThrow().create(net.minecraft.util.RandomSource.create(1L))
                .orElseThrow().getFirst();
        var repair = FlashSaleCatalog.find("repair_scroll_5").orElseThrow().create(net.minecraft.util.RandomSource.create(1L))
                .orElseThrow().getFirst();
        assertEquals(2, FlashSalePreviewMenu.displayCopy(portal, 2).getCount());
        assertEquals(5, FlashSalePreviewMenu.displayCopy(repair, 5).getCount());
        assertEquals(2, portal.getCount());
        assertEquals(5, repair.getCount());
    }

    @Test void nonStackableMultiQuantityPreviewUsesFooterWithoutIllegalStack() {
        var source = new ItemStack(Items.DIAMOND_SWORD);
        var shown = FlashSalePreviewMenu.displayCopy(source, 2);
        assertEquals(1, shown.getCount());
        assertEquals("Offered Quantity: 2", shown.get(net.minecraft.core.component.DataComponents.LORE).lines().getLast().getString());
        assertTrue(shown.get(net.minecraft.core.component.DataComponents.LORE).lines().getLast().getStyle().isBold());
        assertTrue(source.getOrDefault(net.minecraft.core.component.DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore.EMPTY).lines().isEmpty());
    }

    private static Optional<net.minecraft.network.chat.Component> findClickable(net.minecraft.network.chat.Component root) {
        if (root.getStyle().getClickEvent() != null) return Optional.of(root);
        for (var child : root.getSiblings()) {
            var found = findClickable(child);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }
}
