package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.component.EnchantedBlackScrollData;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class EnchantedBlackScrollTest {
    @Test void selectedEligibleEnchantExtractsAtFixedSuccessAndFreshDestroyRate() {
        var fixture = fixture();
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        enchant(sword, fixture.execute(), 5);
        enchant(sword, fixture.doublestrike(), 3);
        ItemStack scroll = new EnchantingRewardItemFactory().enchantedBlackScroll(75);
        var service = new EnchantedBlackScrollExtractionService();
        var candidates = service.candidates(sword);
        assertEquals(2, candidates.size());

        ItemStack output = service.extract(sword, scroll, candidates.getFirst(), 61);

        assertFalse(output.isEmpty());
        var data = output.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        assertNotNull(data);
        assertEquals(candidates.getFirst().id(), data.enchantmentId());
        assertEquals(candidates.getFirst().level(), data.level());
        assertEquals(75, data.successRate());
        assertEquals(61, data.destroyRate());
        assertTrue(scroll.isEmpty());
        assertEquals(1, service.candidates(sword).size());
    }

    @Test void masteryIsExcludedAndInvalidSelectionConsumesNothing() {
        var fixture = fixture();
        ItemStack chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
        enchant(chest, fixture.deathPact(), 5);
        ItemStack scroll = new EnchantingRewardItemFactory().enchantedBlackScroll(50);
        assertTrue(new EnchantedBlackScrollExtractionService().candidates(chest).isEmpty());
        assertEquals(1, scroll.getCount());
    }

    @Test void typedScrollIsNonstackingGlintingAndRateBounded() {
        ItemStack scroll = new EnchantingRewardItemFactory().enchantedBlackScroll(100);
        assertEquals(1, scroll.getMaxStackSize());
        assertTrue(scroll.hasFoil());
        assertEquals(100, scroll.get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get()).returnedSuccessRate());
        assertThrows(IllegalArgumentException.class, () -> new EnchantedBlackScrollData(1, 0));
    }

    @Test void enchantedWordUsesTheExactNineLetterGradient() {
        var letters = EnchantedBlackScrollItem.gradientWord().getSiblings();
        assertEquals(9, letters.size());
        assertEquals(List.of("E", "n", "c", "h", "a", "n", "t", "e", "d"),
                letters.stream().map(Component::getString).toList());
        assertEquals(List.of(0xFFFF00, 0xC8FF00, 0x88FF00, 0x1EFF00, 0x00FFB3,
                        0x00E1FF, 0x0055FF, 0x2F00FF, 0xA200FF),
                letters.stream().map(part -> part.getStyle().getColor().getValue()).toList());
        var rated = new EnchantingRewardItemFactory().enchantedBlackScroll(75).getHoverName();
        assertEquals("75% Enchanted Black Scroll", rated.getString());
        assertTrue(rated.getStyle().isBold());
    }

    @Test void selectionOrderUsesVisibleTransmogOrderAndStillExcludesMastery() {
        var fixture = fixture();
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        enchant(sword, fixture.execute(), 5);       // Elite, inserted first.
        enchant(sword, fixture.doublestrike(), 3); // Legendary, inserted second.
        enchant(sword, fixture.deathPact(), 5);     // Mastery, never selectable.

        var natural = VisibleCosmicEnchantmentOrdering.eligibleBlackScroll(sword);
        assertEquals(2, natural.size());
        sword.set(ModDataComponents.CUSTOM_ENCHANT_META.get(),
                CustomEnchantMetadata.DEFAULT.withTransmogSorted(true));
        var transmog = VisibleCosmicEnchantmentOrdering.eligibleBlackScroll(sword);

        assertEquals(ModEnchantments.DOUBLESTRIKE.identifier(), transmog.getFirst().id());
        assertEquals(ModEnchantments.EXECUTE.identifier(), transmog.get(1).id());
        assertTrue(transmog.stream().noneMatch(entry -> entry.id().equals(ModEnchantments.DEATH_PACT.identifier())));
    }

    private static void enchant(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }
    private static Fixture fixture() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        var execute = registry.register(ModEnchantments.EXECUTE, enchantment("Execute", 5), RegistrationInfo.BUILT_IN);
        var doublestrike = registry.register(ModEnchantments.DOUBLESTRIKE, enchantment("Doublestrike", 3), RegistrationInfo.BUILT_IN);
        var deathPact = registry.register(ModEnchantments.DEATH_PACT, enchantment("Death Pact", 5), RegistrationInfo.BUILT_IN);
        registry.freeze();
        return new Fixture(execute, doublestrike, deathPact);
    }
    private static Enchantment enchantment(String name, int max) {
        var supported = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_SWORD));
        return new Enchantment(Component.literal(name), Enchantment.definition(supported, 1, max,
                Enchantment.constantCost(1), Enchantment.constantCost(1), 1, EquipmentSlotGroup.MAINHAND),
                HolderSet.empty(), DataComponentMap.EMPTY);
    }
    private record Fixture(Holder<Enchantment> execute, Holder<Enchantment> doublestrike,
                           Holder<Enchantment> deathPact) {}
}
