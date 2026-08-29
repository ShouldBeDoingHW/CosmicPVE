package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.data.component.TrialTrinketData;
import com.cosmicpve.data.component.TrialTrinketType;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.trial.trinket.TrialTrinketApplicationService;
import com.cosmicpve.trial.trinket.TrialTrinkets;
import com.cosmicpve.trial.portal.TrialPortalItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.contents.TranslatableContents;

class TrialTrinketFoundationTest {
    @Test void allNineCanonicalVariantsValidate() {
        for (int value : new int[]{1,3,5}) assertTrue(new TrialTrinketData(TrialTrinketType.TIME, value).valid());
        for (int value=1; value<=3; value++) {
            assertTrue(new TrialTrinketData(TrialTrinketType.SKIP, value).valid());
            assertTrue(new TrialTrinketData(TrialTrinketType.INSURANCE, value).valid());
        }
        assertFalse(new TrialTrinketData(TrialTrinketType.TIME, 2).valid());
        assertFalse(new TrialTrinketData(TrialTrinketType.SKIP, 4).valid());
    }

    @Test void maxPortalStoresThreeIndependentCategoriesAndRoundTrips() {
        var portal = TrialPortalModifiers.EMPTY
                .with(new TrialTrinketData(TrialTrinketType.TIME, 5))
                .with(new TrialTrinketData(TrialTrinketType.SKIP, 3))
                .with(new TrialTrinketData(TrialTrinketType.INSURANCE, 3));
        assertEquals(5, portal.timeMinutes()); assertEquals(3, portal.skipRooms()); assertEquals(3, portal.insuranceLevel());
        var json=TrialPortalModifiers.CODEC.encodeStart(JsonOps.INSTANCE, portal).getOrThrow();
        assertEquals(portal, TrialPortalModifiers.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test void strongerReplacesButEqualAndWeakerReject() {
        var portal=TrialPortalModifiers.EMPTY.with(new TrialTrinketData(TrialTrinketType.TIME,3));
        assertEquals(5,portal.with(new TrialTrinketData(TrialTrinketType.TIME,5)).timeMinutes());
        assertThrows(IllegalArgumentException.class,()->portal.with(new TrialTrinketData(TrialTrinketType.TIME,3)));
        assertThrows(IllegalArgumentException.class,()->portal.with(new TrialTrinketData(TrialTrinketType.TIME,1)));
    }

    @Test void timeAppliesOnceToCanonicalInitialTimer() {
        assertEquals(12_000,TrialPortalModifiers.EMPTY.initialTimerTicks());
        assertEquals(13_200,new TrialPortalModifiers(1,1,0,0).initialTimerTicks());
        assertEquals(15_600,new TrialPortalModifiers(1,3,0,0).initialTimerTicks());
        assertEquals(18_000,new TrialPortalModifiers(1,5,0,0).initialTimerTicks());
    }

    @Test void allNineItemsUseOrangeDyePresentation() throws Exception {
        for (String id : new String[]{"time_1","time_3","time_5","skip_1","skip_2","skip_3",
                "insurance_1","insurance_2","insurance_3"}) {
            try (var reader=new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                    "/assets/cosmicpve/items/trial_trinket_"+id+".json")))) {
                assertEquals("minecraft:item/orange_dye",com.google.gson.JsonParser.parseReader(reader).getAsJsonObject()
                        .getAsJsonObject("model").get("model").getAsString());
            }
        }
    }

    @Test void stackedPortalRejectsWithoutMutationOrConsumptionAndSinglePortalBecomesNonStackable() {
        var service = new TrialTrinketApplicationService();
        var stacked = new ItemStack(ModItems.TRIAL_PORTAL.get(), 64);
        var trinket = TrialTrinkets.create(TrialTrinketType.TIME, 5, 1);
        assertEquals(TrialTrinketApplicationService.Outcome.REJECTED_PORTAL_STACK,
                service.apply(trinket, stacked, stacked));
        assertEquals(64, stacked.getCount()); assertEquals(1, trinket.getCount());
        assertFalse(stacked.has(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()));

        var portal = new ItemStack(ModItems.TRIAL_PORTAL.get());
        assertEquals(TrialTrinketApplicationService.Outcome.SUCCESS, service.apply(trinket, portal, portal));
        assertEquals(0, trinket.getCount()); assertEquals(1, portal.getMaxStackSize());
        assertEquals(1, portal.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
        assertEquals(5, portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()).timeMinutes());
        ItemStack savedCopy = portal.copy();
        assertEquals(1, savedCopy.getMaxStackSize());
        assertEquals(portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()),
                savedCopy.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()));
        assertTrue(ItemStack.isSameItemSameComponents(portal, savedCopy));
        assertFalse(ItemStack.isSameItemSameComponents(portal, new ItemStack(ModItems.TRIAL_PORTAL.get())));
        var malformedCommandStack = new ItemStack(ModItems.TRIAL_PORTAL.get(), 2);
        assertThrows(IllegalArgumentException.class, () -> com.cosmicpve.trial.portal.TrialPortalItem.applyModifiers(
                malformedCommandStack, new TrialPortalModifiers(1, 5, 0, 0)));
    }

    @Test void trinketFamiliesExposeCanonicalPresentationColorsWithoutChangingValues() {
        assertEquals(0x2BC2B8, TrialTrinketType.SKIP.presentationColor());
        assertEquals(0x0A5751, TrialTrinketType.TIME.presentationColor());
        assertEquals(0x0A5721, TrialTrinketType.INSURANCE.presentationColor());
        for (var type : TrialTrinketType.values()) {
            int value = type == TrialTrinketType.TIME ? 5 : 3;
            var stack = TrialTrinkets.create(type, value, 1);
            assertEquals(type.presentationColor(), stack.getHoverName().getStyle().getColor().getValue());
            assertTrue(stack.getHoverName().getStyle().isBold());
            assertEquals(value, stack.get(ModDataComponents.TRIAL_TRINKET.get()).value());
        }
        var lines = com.cosmicpve.trial.portal.TrialPortalItem.modifierLines(
                new TrialPortalModifiers(1, 5, 3, 3));
        assertEquals(6, lines.size());
        assertEquals(0xFFAA00, lines.get(0).getStyle().getColor().getValue());
        assertEquals(0x2BC2B8, lines.get(0).getSiblings().getFirst().getStyle().getColor().getValue());
        assertEquals(0x0A5751, lines.get(2).getSiblings().getFirst().getStyle().getColor().getValue());
        assertEquals(0x0A5721, lines.get(4).getSiblings().getFirst().getStyle().getColor().getValue());
        assertTrue(lines.get(0).getSiblings().getFirst().getStyle().isBold());
        assertTrue(lines.get(2).getSiblings().getFirst().getStyle().isBold());
        assertTrue(lines.get(4).getSiblings().getFirst().getStyle().isBold());
        assertEquals(0x777777, lines.get(1).getSiblings().getFirst().getStyle().getColor().getValue());
    }

    @Test void portalTooltipIsInformativeOrderedAndDataPureForEmptyAndFullyModifiedPortals() {
        var empty = com.cosmicpve.trial.portal.TrialPortalItem.tooltipLines(TrialPortalModifiers.EMPTY);
        assertEquals(5, empty.size());
        assertTrue(empty.get(3).getStyle().isBold());
        assertEquals(0xFFAA00, empty.get(3).getStyle().getColor().getValue());
        assertEquals(0x777777, empty.get(4).getStyle().getColor().getValue());
        assertFalse(empty.toString().contains("/trials"));

        var modifiers = new TrialPortalModifiers(1, 5, 3, 3);
        var before = modifiers;
        var full = com.cosmicpve.trial.portal.TrialPortalItem.tooltipLines(modifiers);
        assertEquals(10, full.size());
        assertEquals(0x2BC2B8, full.get(4).getSiblings().getFirst().getStyle().getColor().getValue());
        assertEquals(0x0A5751, full.get(6).getSiblings().getFirst().getStyle().getColor().getValue());
        assertEquals(0x0A5721, full.get(8).getSiblings().getFirst().getStyle().getColor().getValue());
        assertTrue(full.get(4).toString().contains("skip"));
        assertTrue(full.get(6).toString().contains("time"));
        assertTrue(full.get(8).toString().contains("insurance"));
        assertFalse(full.toString().contains("/trials"));
        assertEquals(before, modifiers);
    }

    @Test void everyCanonicalModifierValueIsRenderedThroughItsFamilyMetadata() {
        for (int skip = 1; skip <= 3; skip++) {
            assertEquals(skip, modifierArgument(TrialPortalItem.modifierLines(
                    new TrialPortalModifiers(1, 0, skip, 0)).getFirst()));
        }
        for (int time : new int[]{1, 3, 5}) {
            assertEquals(time, modifierArgument(TrialPortalItem.modifierLines(
                    new TrialPortalModifiers(1, time, 0, 0)).getFirst()));
        }
        for (int insurance = 1; insurance <= 3; insurance++) {
            assertEquals(insurance, modifierArgument(TrialPortalItem.modifierLines(
                    new TrialPortalModifiers(1, 0, 0, insurance)).getFirst()));
        }
    }

    private static int modifierArgument(net.minecraft.network.chat.Component bulletLine) {
        var contents = (TranslatableContents) bulletLine.getSiblings().getFirst().getContents();
        return ((Number) contents.getArgs()[0]).intValue();
    }
}
