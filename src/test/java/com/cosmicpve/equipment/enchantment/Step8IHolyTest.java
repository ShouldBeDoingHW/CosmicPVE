package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class Step8IHolyTest {
    @Test void presentationIsFullyBoldAndHolyMarkerRetainsCanonicalEmphasis() {
        var holyItem = ModItems.HOLY_WHITE_SCROLL.get();
        var name = holyItem.getName(new ItemStack(holyItem));
        assertEquals("* Holy * Whitescroll", name.getString());
        assertTrue(name.getStyle().isBold());
        assertEquals(0xC4394A, name.getStyle().getColor().getValue());
        assertTrue(name.getSiblings().stream().allMatch(part -> part.getStyle().isBold()));
        assertTrue(name.getSiblings().getFirst().getStyle().isUnderlined());
        assertEquals(0xFFFFFF, name.getSiblings().getLast().getStyle().getColor().getValue());

        var marker = com.cosmicpve.equipment.EquipmentTooltipService.holyMarker();
        assertEquals("HOLY", marker.getString());
        assertTrue(marker.getStyle().isBold());
        assertTrue(marker.getStyle().isUnderlined());
        assertEquals(0xC4394A, marker.getStyle().getColor().getValue());
    }

    @Test void eligibilityIsExactAndProbabilityHasFutureMonopolySeam() {
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_HELMET)));
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_SWORD)));
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_AXE)));
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.BOW)));
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.CROSSBOW)));
        assertTrue(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertFalse(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_SHOVEL)));
        assertFalse(HolyWhiteScrollService.eligible(new ItemStack(Items.DIAMOND_HOE)));
        assertFalse(HolyWhiteScrollService.eligible(new ItemStack(Items.SHIELD)));
        assertFalse(HolyWhiteScrollService.eligible(new ItemStack(Items.ELYTRA)));
        assertEquals(.50, HolyWhiteScrollService.preservationChance(false));
        assertEquals(.55, HolyWhiteScrollService.preservationChance(true));
    }

    @Test void applicationConsumesWhiteProtectionAndAllowsFreshProtectionLater() {
        ItemStack target = new ItemStack(Items.DIAMOND_SWORD);
        var white = new WhiteScrollProtectionService();
        assertTrue(white.apply(target));
        ItemStack holyScroll = new ItemStack(ModItems.HOLY_WHITE_SCROLL.get());
        assertEquals(HolyWhiteScrollService.Outcome.SUCCESS,
                new HolyWhiteScrollService().apply(holyScroll, target, target));
        assertEquals(0, holyScroll.getCount());
        assertTrue(HolyWhiteScrollService.isHoly(target));
        assertFalse(white.isProtected(target));
        assertTrue(white.apply(target));
        assertTrue(white.isProtected(target));
        assertTrue(HolyWhiteScrollService.isHoly(target));
        HolyWhiteScrollService.consumeHoly(target);
        assertFalse(HolyWhiteScrollService.isHoly(target));
        assertTrue(white.isProtected(target));
    }

    @Test void rejectionIsNonMutating() {
        ItemStack target = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack holyScroll = new ItemStack(ModItems.HOLY_WHITE_SCROLL.get(), 2);
        assertEquals(HolyWhiteScrollService.Outcome.REJECTED_UNPROTECTED,
                new HolyWhiteScrollService().apply(holyScroll, target, target));
        assertEquals(2, holyScroll.getCount());
        assertFalse(target.has(ModDataComponents.HOLY.get()));
    }
}
