package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import com.cosmicpve.tinkerer.TinkererSalvageService;
import org.junit.jupiter.api.Test;

class CosmicDustFoundationTest {
    @Test void salvageFormulaUsesCompletedTensAndCapsAtTen() {
        assertEquals(2, CosmicDustService.yield(1, 9));
        assertEquals(3, CosmicDustService.yield(1, 10));
        assertEquals(3, CosmicDustService.yield(1, 19));
        assertEquals(6, CosmicDustService.yield(2, 37));
        assertEquals(10, CosmicDustService.yield(4, 80));
        assertEquals(10, CosmicDustService.yield(1, 100));
    }

    @Test void matchingDustBulkAppliesOnlyUsefulAmountAndPreservesBookFields() {
        ItemStack book = book(CosmicPVE.id("doublestrike"), 3, 97, 73);
        ItemStack dust = CosmicDustService.dust(CosmicEnchantmentTier.LEGENDARY, 20);
        var result = new CosmicDustService().apply(dust, book, book);
        assertEquals(CosmicDustService.ApplicationOutcome.SUCCESS, result.outcome());
        assertEquals(3, result.consumed());
        assertEquals(17, dust.getCount());
        var after = book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        assertEquals(CosmicPVE.id("doublestrike"), after.enchantmentId());
        assertEquals(3, after.level());
        assertEquals(100, after.successRate());
        assertEquals(73, after.destroyRate());
        assertEquals(1, after.dataVersion());
    }

    @Test void oneMatchingDustAddsOneFlatSuccessPoint() {
        ItemStack book = book(CosmicPVE.id("molten"), 4, 43, 80);
        ItemStack dust = CosmicDustService.dust(CosmicEnchantmentTier.UNIQUE, 1);
        var result = new CosmicDustService().apply(dust, book, book);
        assertEquals(CosmicDustService.ApplicationOutcome.SUCCESS, result.outcome());
        assertEquals(1, result.consumed());
        assertTrue(dust.isEmpty());
        assertEquals(44, book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()).successRate());
        assertEquals(80, book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()).destroyRate());
    }

    @Test void masteryDustStopsAtFifty() {
        ItemStack book = book(CosmicPVE.id("death_pact"), 5, 48, 90);
        ItemStack dust = CosmicDustService.dust(CosmicEnchantmentTier.MASTERY, 10);
        var result = new CosmicDustService().apply(dust, book, book);
        assertEquals(2, result.consumed());
        assertEquals(8, dust.getCount());
        assertEquals(50, book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()).successRate());
        assertEquals(CosmicDustService.ApplicationOutcome.REJECTED_CAPPED,
                new CosmicDustService().apply(dust, book, book).outcome());
        assertEquals(8, dust.getCount());
    }

    @Test void wrongRarityAndStaleTargetsConsumeNothing() {
        ItemStack book = book(CosmicPVE.id("molten"), 4, 43, 80);
        ItemStack dust = CosmicDustService.dust(CosmicEnchantmentTier.ELITE, 7);
        assertEquals(CosmicDustService.ApplicationOutcome.REJECTED_RARITY,
                new CosmicDustService().apply(dust, book, book).outcome());
        assertEquals(7, dust.getCount());
        assertEquals(43, book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get()).successRate());
        assertEquals(CosmicDustService.ApplicationOutcome.REJECTED_STALE,
                new CosmicDustService().apply(dust, book.copy(), book).outcome());
    }

    @Test void dustIsStackableTypedAndUsesSugarTintModel() throws Exception {
        ItemStack stack = CosmicDustService.dust(CosmicEnchantmentTier.UNIQUE, 64);
        assertEquals(64, stack.getMaxStackSize());
        assertEquals(CosmicEnchantmentTier.UNIQUE, stack.get(ModDataComponents.COSMIC_DUST.get()).tier());
        try (var reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/cosmic_dust.json")))) {
            assertEquals("minecraft:item/sugar", JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("textures").get("layer0").getAsString());
        }
    }

    @Test void unexaminedAndVanillaBooksAreNotSalvageable() {
        assertTrue(CosmicDustService.bookTier(new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK)).isEmpty());
        assertTrue(CosmicDustService.bookTier(new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get())).isEmpty());
    }

    @Test void everyCosmicRarityIsSalvageableButUnrelatedItemsAreRejected() {
        assertEquals(CosmicEnchantmentTier.SIMPLE,
                CosmicDustService.bookTier(book(CosmicPVE.id("lightning"), 1, 25, 25)).orElseThrow());
        assertEquals(CosmicEnchantmentTier.UNIQUE,
                CosmicDustService.bookTier(book(CosmicPVE.id("molten"), 1, 25, 25)).orElseThrow());
        assertEquals(CosmicEnchantmentTier.ELITE,
                CosmicDustService.bookTier(book(CosmicPVE.id("execute"), 1, 25, 25)).orElseThrow());
        assertEquals(CosmicEnchantmentTier.ULTIMATE,
                CosmicDustService.bookTier(book(CosmicPVE.id("angelic"), 1, 25, 25)).orElseThrow());
        assertEquals(CosmicEnchantmentTier.LEGENDARY,
                CosmicDustService.bookTier(book(CosmicPVE.id("doublestrike"), 1, 25, 25)).orElseThrow());
        assertEquals(CosmicEnchantmentTier.MASTERY,
                CosmicDustService.bookTier(book(CosmicPVE.id("death_pact"), 1, 25, 75)).orElseThrow());
        assertTrue(CosmicDustService.bookTier(new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)).isEmpty());
        assertTrue(CosmicDustService.bookTier(new ItemStack(ModItems.WHITE_SCROLL.get())).isEmpty());
        assertTrue(CosmicDustService.bookTier(new ItemStack(ModItems.BLACK_SCROLL.get())).isEmpty());
    }

    @Test void confirmationAggregatesRaritiesConsumesOnceAndIgnoresInvalidItems() {
        var input = new SimpleContainer(5);
        input.setItem(0, new ItemStack(net.minecraft.world.item.Items.RED_STAINED_GLASS_PANE));
        input.setItem(1, book(CosmicPVE.id("molten"), 4, 80, 1));
        input.setItem(2, book(CosmicPVE.id("ender_shift"), 2, 37, 100));
        input.setItem(3, new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK));
        var service = new TinkererSalvageService();
        var result = service.confirm(input, 1, 5);
        assertTrue(result.succeeded());
        assertEquals(2, result.consumedBooks());
        assertEquals(16, result.dust().get(CosmicEnchantmentTier.UNIQUE));
        assertTrue(input.getItem(1).isEmpty());
        assertTrue(input.getItem(2).isEmpty());
        assertTrue(input.getItem(3).is(net.minecraft.world.item.Items.ENCHANTED_BOOK));
        var replay = service.confirm(input, 1, 5);
        assertFalse(replay.succeeded());
        assertTrue(replay.dust().isEmpty());
    }

    private static ItemStack book(net.minecraft.resources.Identifier id, int level, int success, int destroy) {
        ItemStack stack = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(1, id, level, success, destroy));
        return stack;
    }
}
