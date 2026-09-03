package com.cosmicpve.economy;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SellServiceTest {
    @Test void registryContainsExactCanonicalValuesAndAllSixteenWoolsOnly() {
        assertEquals(56, SellPriceRegistry.prices().size());
        assertPrice(Items.BEEF, 275); assertPrice(Items.COOKED_BEEF, 475);
        assertPrice(Items.ROTTEN_FLESH, 245); assertPrice(Items.MUTTON, 315);
        assertPrice(Items.COOKED_MUTTON, 515); assertPrice(Items.ENDER_PEARL, 750);
        assertPrice(Items.GUNPOWDER, 815); assertPrice(Items.FEATHER, 215);
        for (var wool : List.of(Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL,
                Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL,
                Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL,
                Items.RED_WOOL, Items.BLACK_WOOL)) assertPrice(wool, 100);
        assertTrue(SellPriceRegistry.price(Items.WHITE_CARPET).isEmpty());
    }

    @Test void sellHandPlanCombinesEveryMatchingEligibleStackWithExactCents() {
        var plan = SellService.plan(List.of(new ItemStack(Items.BEEF, 7), new ItemStack(Items.BEEF, 10),
                new ItemStack(Items.GUNPOWDER, 4)), Items.BEEF);
        assertEquals(1, plan.lines().size());
        assertEquals(17, plan.lines().getFirst().quantity());
        assertEquals(4_675, plan.totalCents());
    }

    @Test void sellAllUsesExactMixedArithmeticAndLeavesUnsellableOutOfPlan() {
        var plan = SellService.plan(List.of(new ItemStack(Items.GUNPOWDER, 3), new ItemStack(Items.GOLD_INGOT, 2),
                new ItemStack(Items.BLUE_WOOL, 5), new ItemStack(Items.DIAMOND, 9)), null);
        assertEquals(3, plan.lines().size());
        assertEquals(5_445, plan.totalCents());
        assertTrue(plan.lines().stream().noneMatch(line -> line.item() == Items.DIAMOND));
    }

    @Test void recognizedCosmicIdentityPreventsCommodityCollision() {
        ItemStack customBeef = new ItemStack(Items.BEEF, 12);
        customBeef.set(ModDataComponents.HOLY.get(), true);
        assertFalse(SellService.eligible(customBeef));
        var plan = SellService.plan(List.of(customBeef, new ItemStack(Items.BEEF, 5)), null);
        assertEquals(5, plan.lines().getFirst().quantity());
        assertEquals(1_375, plan.totalCents());
    }

    @Test void registryIterationOrderIsStableForCompactSellAllSummaries() {
        assertEquals(List.of(Items.BEEF, Items.COOKED_BEEF, Items.ROTTEN_FLESH, Items.BONE),
                SellPriceRegistry.prices().keySet().stream().limit(4).toList());
    }

    private static void assertPrice(net.minecraft.world.item.Item item, long cents) {
        assertEquals(cents, SellPriceRegistry.price(item).orElseThrow());
    }
}
