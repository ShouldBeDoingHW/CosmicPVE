package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.*;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class ArmorSetCrystalDataTest {
    private static ArmorSetIdentity identity() {
        return new ArmorSetIdentity(1, ArmorSetIds.PHANTOM, Component.literal("Phantom"), 0xFF6969,
                List.of(Component.literal("bonus")));
    }
    @Test void acceptsOnlyOneThroughOneHundred() {
        assertThrows(IllegalArgumentException.class, () -> new ArmorSetCrystalData(1, identity(), 0));
        assertThrows(IllegalArgumentException.class, () -> new ArmorSetCrystalData(1, identity(), 101));
        assertEquals(1, new ArmorSetCrystalData(1, identity(), 1).successRate());
        assertEquals(100, new ArmorSetCrystalData(1, identity(), 100).successRate());
    }
    @Test void hundredAlwaysSucceedsWithoutRolling() {
        assertTrue(ArmorCrystalApplicationService.rollSucceeds(100, () -> { throw new AssertionError("rolled"); }));
    }
    @Test void oneRollControlsOrdinarySuccess() {
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        assertTrue(ArmorCrystalApplicationService.rollSucceeds(50, () -> { calls.incrementAndGet(); return 50; }));
        assertEquals(1, calls.get());
        assertFalse(ArmorCrystalApplicationService.rollSucceeds(50, () -> 51));
    }
    @Test void crystalNamesAreBoldWithOrWithoutResolvedIdentity() {
        var crystal = new ItemStack(com.cosmicpve.registry.ModItems.ARMOR_SET_CRYSTAL.get());
        assertTrue(crystal.getHoverName().getStyle().isBold());
        crystal.set(com.cosmicpve.registry.ModDataComponents.ARMOR_SET_CRYSTAL.get(),
                new ArmorSetCrystalData(1, identity(), 75));
        assertTrue(crystal.getHoverName().getStyle().isBold());
        assertEquals(identity().color(), crystal.getHoverName().getStyle().getColor().getValue());
    }
    @Test void staleTargetIsRejectedBeforeMutationOrRoll() {
        var expected = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE);
        var current = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE);
        var service = new ArmorCrystalApplicationService(null, () -> { throw new AssertionError("rolled"); });
        assertEquals(ArmorCrystalApplicationService.Outcome.STALE_TARGET,
                service.apply(net.minecraft.world.item.ItemStack.EMPTY, expected, current));
        assertEquals(1, expected.getCount());
        assertEquals(1, current.getCount());
    }
}
