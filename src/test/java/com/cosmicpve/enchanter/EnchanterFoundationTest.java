package com.cosmicpve.enchanter;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class EnchanterFoundationTest {
    @Test void exactFiveOffersOccupyAlternatingSlotsWithCanonicalIconsCostsAndColors() {
        assertEquals(9, EnchanterMenu.SLOT_COUNT);
        assertEquals(List.of(
                new EnchanterOffer(0, CosmicEnchantmentTier.SIMPLE, 400, Items.WHITE_STAINED_GLASS_PANE, 0xFFFFFF),
                new EnchanterOffer(2, CosmicEnchantmentTier.UNIQUE, 1_000, Items.LIME_STAINED_GLASS_PANE, 0x55FF55),
                new EnchanterOffer(4, CosmicEnchantmentTier.ELITE, 1_800, Items.CYAN_STAINED_GLASS_PANE, 0xA3FFF5),
                new EnchanterOffer(6, CosmicEnchantmentTier.ULTIMATE, 3_500, Items.YELLOW_STAINED_GLASS_PANE, 0xFFFF55),
                new EnchanterOffer(8, CosmicEnchantmentTier.LEGENDARY, 6_000, Items.ORANGE_STAINED_GLASS_PANE, 0xFFAA00)),
                EnchanterOffer.ALL);
        assertNull(EnchanterOffer.at(1));
        assertNull(EnchanterOffer.at(3));
        assertNull(EnchanterOffer.at(5));
        assertNull(EnchanterOffer.at(7));
        assertFalse(EnchanterOffer.ALL.stream().anyMatch(offer ->
                offer.tier() == CosmicEnchantmentTier.MASTERY || offer.tier() == CosmicEnchantmentTier.HEROIC));
    }

    @Test void offerPresentationShowsExactBalanceCostAndSemanticStyles() {
        ItemStack stack = EnchanterMenu.offer(EnchanterOffer.ALL.get(1), 2_275);
        assertEquals(Items.LIME_STAINED_GLASS_PANE, stack.getItem());
        assertEquals("Unique Enchantment Book", stack.getHoverName().getString());
        assertTrue(stack.getHoverName().getStyle().isBold());
        assertEquals(0x55FF55, stack.getHoverName().getStyle().getColor().getValue());
        var lore = stack.get(DataComponents.LORE).lines();
        assertEquals(List.of("UNEXAMINED BOOK", "Cost: 1,000 XP", "You Have: 2,275 XP", "Click to purchase one."),
                lore.stream().map(component -> component.getString()).toList());
        assertTrue(lore.getFirst().getStyle().isBold());
        assertTrue(lore.getLast().getStyle().isItalic());
    }

    @Test void exactRawXpExampleConstructsDebitsAndDeliversOncePerPurchase() {
        AtomicInteger balance = new AtomicInteger(2_675);
        List<CosmicEnchantmentTier> delivered = new ArrayList<>();
        for (CosmicEnchantmentTier tier : List.of(
                CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentTier.SIMPLE)) {
            int cost = EnchanterOffer.ALL.stream().filter(offer -> offer.tier() == tier).findFirst().orElseThrow().cost();
            var outcome = EnchanterPurchaseTransaction.execute(balance.get(), cost,
                    () -> new ItemStack(Items.BOOK), debit -> {
                        if (balance.get() < debit) return false;
                        balance.addAndGet(-debit);
                        return true;
                    }, ignored -> delivered.add(tier));
            assertEquals(EnchanterPurchaseTransaction.Outcome.SUCCESS, outcome);
        }
        assertEquals(275, balance.get());
        assertEquals(List.of(CosmicEnchantmentTier.UNIQUE, CosmicEnchantmentTier.UNIQUE,
                CosmicEnchantmentTier.SIMPLE), delivered);
    }

    @Test void insufficientXpPerformsNoConstructionDebitOrDelivery() {
        AtomicInteger calls = new AtomicInteger();
        var outcome = EnchanterPurchaseTransaction.execute(399, 400,
                () -> { calls.incrementAndGet(); return new ItemStack(Items.BOOK); },
                ignored -> { calls.incrementAndGet(); return true; }, ignored -> calls.incrementAndGet());
        assertEquals(EnchanterPurchaseTransaction.Outcome.INSUFFICIENT_XP, outcome);
        assertEquals(0, calls.get());
    }
}
