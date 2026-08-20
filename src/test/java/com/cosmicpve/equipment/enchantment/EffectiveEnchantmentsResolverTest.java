package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.cosmicpve.registry.ModEnchantments;
import org.junit.jupiter.api.Test;

class EffectiveEnchantmentsResolverTest {
    private static final net.minecraft.resources.Identifier TEST_ENCHANT = CosmicPVE.id("test_enchant");

    @Test
    void actualAndVirtualSourcesResolveToDeterministicMaximum() {
        var resolver = new EffectiveEnchantmentsResolver();
        var resolved = resolver.resolveSources(
                List.of(new ActualEnchantmentGrant(TEST_ENCHANT, 2, CosmicPVE.id("actual_weapon"))),
                List.of(
                        new VirtualEnchantmentGrant(TEST_ENCHANT, 1, CosmicPVE.id("skin_b")),
                        new VirtualEnchantmentGrant(TEST_ENCHANT, 3, CosmicPVE.id("skin_a"))));

        var enchantment = resolved.get(TEST_ENCHANT).orElseThrow();
        assertEquals(3, enchantment.level());
        assertEquals(3, enchantment.provenance().size());
        assertEquals(EnchantmentSourceKind.ACTUAL, enchantment.provenance().getFirst().kind());
        assertTrue(enchantment.hasVirtualSource());
    }

    @Test
    void virtualGrantDoesNotMutateUnderlyingItem() {
        var stack = new ItemStack(Items.IRON_AXE);
        var before = stack.copy();

        var resolved = new EffectiveEnchantmentsResolver().resolve(
                stack,
                List.of(new VirtualEnchantmentGrant(TEST_ENCHANT, 3, CosmicPVE.id("boosted_chainsaw"))));

        assertEquals(3, resolved.level(TEST_ENCHANT));
        assertTrue(ItemStack.matches(before, stack));
        assertFalse(net.minecraft.world.item.enchantment.EnchantmentHelper.hasAnyEnchantments(stack));
    }

    @Test
    void boostedChainsawStyleVirtualDoublestrikeDoesNotMutateAxe() {
        var axe = new ItemStack(Items.DIAMOND_AXE);
        var before = axe.copy();
        var resolved = new EffectiveEnchantmentsResolver().resolve(
                axe, List.of(new VirtualEnchantmentGrant(
                        ModEnchantments.DOUBLESTRIKE.identifier(), 3, CosmicPVE.id("boosted_chainsaw"))));

        assertEquals(3, resolved.level(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertTrue(ItemStack.matches(before, axe));
        assertFalse(EnchantmentHelper.hasAnyEnchantments(axe));
    }
}
