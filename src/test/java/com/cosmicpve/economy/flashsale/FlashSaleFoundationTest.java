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
    @Test void completeCanonicalCatalogHasExactRowsAndPricesInCents() {
        var expected = List.of(
                "trial_portal_1|1|20000000|35000000|50000000",
                "trial_portal_2|2|35000000|55000000|87500000",
                "memory_chest|1|250000000|350000000|450000000",
                "armor_orb_100|1|30000000|50000000|60000000",
                "weapon_orb_100|1|30000000|50000000|60000000",
                "engineer_crystal_50|1|240000000|280000000|320000000",
                "phantom_crystal_50|1|240000000|280000000|320000000",
                "ranger_crystal_50|1|240000000|280000000|320000000",
                "dragonslayer_crystal_50|1|240000000|280000000|320000000",
                "yeti_crystal_75|1|280000000|320000000|360000000",
                "dimensional_traveler_crystal_75|1|280000000|320000000|360000000",
                "yjiki_crystal_75|1|280000000|320000000|360000000",
                "ancient_crystal_75|1|280000000|320000000|360000000",
                "abandoned_spaceship_portal|1|150000000|160000000|170000000",
                "mystery_elite_spawner|1|60000000|80000000|100000000",
                "mystery_simple_spawner|1|25000000|30000000|35000000",
                "repair_scroll_1|1|5000000|7000000|9000000",
                "repair_scroll_5|5|20000000|40000000|60000000",
                "heroic_crystal|1|45000000|65000000|77500000",
                "godly_vkit_bundle|1|600000000|700000000|800000000",
                "cosmic_enchantment_table|1|90000000|100000000|120000000",
                "white_scroll|1|10000000|12500000|17500000",
                "mystery_call_of_adventure|1|90000000|115000000|145000000",
                "trials_creation_kit|1|250000000|280000000|310000000",
                "heroic_cosmic_enchantment_table|1|160000000|180000000|200000000",
                "space_dust_bundle|1|17500000|22500000|25000000",
                "amulet_socket_40|1|120000000|140000000|160000000",
                "belt_socket_40|1|120000000|140000000|160000000",
                "omni_socket_40|1|140000000|160000000|180000000");
        assertEquals(expected, FlashSaleCatalog.CANONICAL_ROWS.stream().map(row -> row.id() + "|" + row.quantity()
                + "|" + row.lowPrice() + "|" + row.mediumPrice() + "|" + row.highPrice()).toList());
        assertEquals(29, expected.stream().map(row -> row.substring(0, row.indexOf('|'))).distinct().count());
    }

    @Test void catalogPreservesCanonicalRowsAndExplicitDeferredDependencies() {
        assertEquals(29, FlashSaleCatalog.CANONICAL_ROWS.size());
        assertEquals(27, FlashSaleCatalog.productionRows().size());
        assertFalse(FlashSaleCatalog.find("abandoned_spaceship_portal").orElseThrow().productionSelectable());
        assertFalse(FlashSaleCatalog.find("trials_creation_kit").orElseThrow().productionSelectable());
        assertEquals("memory_chest", FlashSaleCatalog.MEMORY_CHEST.id());
        assertTrue(FlashSaleCatalog.MEMORY_CHEST.productionSelectable());
        assertEquals(250_000_000L, FlashSaleCatalog.MEMORY_CHEST.lowPrice());
        assertEquals(350_000_000L, FlashSaleCatalog.MEMORY_CHEST.mediumPrice());
        assertEquals(450_000_000L, FlashSaleCatalog.MEMORY_CHEST.highPrice());
    }

    @Test void exactCanonicalPricesAndDuplicateFamiliesRemainSeparateRows() {
        var portalOne = FlashSaleCatalog.find("trial_portal_1").orElseThrow();
        var portalTwo = FlashSaleCatalog.find("trial_portal_2").orElseThrow();
        assertEquals(1, portalOne.quantity()); assertEquals(2, portalTwo.quantity());
        assertEquals(20_000_000L, portalOne.lowPrice()); assertEquals(87_500_000L, portalTwo.highPrice());
        assertEquals(2, FlashSaleCatalog.CANONICAL_ROWS.stream().filter(row -> row.displayName().getString().equals("Repair Scroll")).count());
        var whiteScroll = FlashSaleCatalog.find("white_scroll").orElseThrow();
        assertEquals(1, whiteScroll.quantity());
        assertEquals(10_000_000L, whiteScroll.lowPrice());
        assertEquals(12_500_000L, whiteScroll.mediumPrice());
        assertEquals(17_500_000L, whiteScroll.highPrice());
        assertTrue(whiteScroll.create(net.minecraft.util.RandomSource.create(1L)).orElseThrow().getFirst().is(com.cosmicpve.registry.ModItems.WHITE_SCROLL.get()));
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
