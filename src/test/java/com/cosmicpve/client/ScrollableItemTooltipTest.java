package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ScrollableItemTooltipTest {
    @Test void onlyOverflowingTooltipsHaveScrollableDistance() {
        assertEquals(0,ScrollableItemTooltip.overflow(8,100,240));
        assertEquals(68,ScrollableItemTooltip.overflow(4,300,240));
        assertEquals(118,ScrollableItemTooltip.overflow(54,300,240));
        assertEquals(0,ScrollableItemTooltip.overflow(4,232,240));
    }
    @Test void scrollClampsAndIdentityOrScreenChangesResetOffset() {
        var state=new ScrollableItemTooltip.State(); var firstScreen=new Object(); var secondScreen=new Object();
        state.update(firstScreen,1,25);
        assertTrue(state.scroll(firstScreen,-1)); assertEquals(10,state.offsetPixels());
        state.scroll(firstScreen,-1); state.scroll(firstScreen,-1); state.scroll(firstScreen,-1);
        assertEquals(25,state.offsetPixels());
        state.update(firstScreen,2,25); assertEquals(0,state.offsetPixels());
        state.scroll(firstScreen,-1); state.update(secondScreen,2,25); assertEquals(0,state.offsetPixels());
        assertFalse(state.scroll(firstScreen,-1));
    }
    @Test void repeatedWheelStepsReachRealGeometricBottomAndReturnToTop() {
        var state=new ScrollableItemTooltip.State(); var screen=new Object(); state.update(screen,1,118);
        for(int i=0;i<20;i++) assertTrue(state.scroll(screen,-1));
        assertEquals(118,state.offsetPixels());
        for(int i=0;i<20;i++) assertTrue(state.scroll(screen,1));
        assertEquals(0,state.offsetPixels());
    }
    @Test void onlyMatchingMaskIdentityBecomesANonWrappingTooltipComponent() {
        var other=net.minecraft.network.chat.Component.literal("Other");
        var identity=net.minecraft.network.chat.Component.literal("ATTACHED: Multi-Mask (A, B, C, D, E)");
        var elements=new java.util.ArrayList<com.mojang.datafixers.util.Either<net.minecraft.network.chat.FormattedText,
                net.minecraft.world.inventory.tooltip.TooltipComponent>>();
        elements.add(com.mojang.datafixers.util.Either.left(other));
        elements.add(com.mojang.datafixers.util.Either.left(identity));
        assertTrue(ScrollableItemTooltip.replaceIdentityElement(elements,identity));
        assertTrue(elements.getFirst().left().isPresent());
        assertInstanceOf(com.cosmicpve.equipment.mask.MaskLore.AttachedIdentityTooltip.class,
                elements.get(1).right().orElseThrow());
        assertFalse(ScrollableItemTooltip.replaceIdentityElement(elements,
                net.minecraft.network.chat.Component.literal("Missing")));
    }
    @Test void ordinaryWheelRemainsUntouchedWhenTooltipDoesNotOverflow() {
        var state=new ScrollableItemTooltip.State(); var screen=new Object(); state.update(screen,1,0);
        assertFalse(state.scroll(screen,-1)); assertFalse(state.scroll(screen,1)); assertEquals(0,state.offsetPixels());
    }
}
