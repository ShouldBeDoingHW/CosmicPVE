package com.cosmicpve.economy.fame;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class FameShopFoundationTest {
    @Test void catalogsPersistNineOffersForTenDays() {
        var offers = java.util.stream.IntStream.range(0,9).mapToObj(i -> new FameShopOffer(
                com.cosmicpve.trial.TrialPhase.APPRENTICE, "cosmicpve:trial/apprentice#" + i,
                List.of(new ItemStack(Items.APPLE, i + 1)), 20, false)).toList();
        var catalog = new FameShopCatalog(1, 40, 50, offers);
        assertTrue(catalog.current(49)); assertFalse(catalog.current(50));
        assertTrue(catalog.purchase(2).offers().get(2).purchased());
        assertFalse(catalog.offers().get(2).purchased());
        assertEquals("cosmicpve:trial/apprentice#2", catalog.purchase(2).offers().get(2).sourceRow());
    }
    @Test void exactPayloadUniquenessUsesComponentsCountsAndBundles() {
        assertTrue(FameShopService.samePayload(List.of(new ItemStack(Items.APPLE,2)), List.of(new ItemStack(Items.APPLE,2))));
        assertFalse(FameShopService.samePayload(List.of(new ItemStack(Items.APPLE,1)), List.of(new ItemStack(Items.APPLE,2))));
        assertFalse(FameShopService.samePayload(List.of(new ItemStack(Items.APPLE)), List.of(new ItemStack(Items.CARROT))));
    }
    @Test void pricesRemainBoundToExactSourceTier() {
        assertEquals(20, FameShopService.priceFor(com.cosmicpve.trial.TrialPhase.APPRENTICE));
        assertEquals(35, FameShopService.priceFor(com.cosmicpve.trial.TrialPhase.HARDCORE));
        assertEquals(55, FameShopService.priceFor(com.cosmicpve.trial.TrialPhase.IMPOSSIBLE));
        assertEquals(75, FameShopService.priceFor(com.cosmicpve.trial.TrialPhase.DEMONIC));
    }
}
