package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.Lifecycle;
import java.util.concurrent.atomic.AtomicInteger;
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

class HeroicBookApplicationTest {
    @Test void initialConversionRequiresExactMaximumBeforeAnyRoll() {
        var fixture = fixture();
        var calls = new AtomicInteger();
        var service = service(fixture, () -> { calls.incrementAndGet(); return 1; });
        ItemStack noBleed = new ItemStack(Items.DIAMOND_AXE);
        ItemStack noBleedBook = book(3, 100, 100);
        assertEquals(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE,
                service.apply(noBleedBook, noBleed, noBleed).outcome());
        ItemStack lowBleed = new ItemStack(Items.DIAMOND_AXE);
        set(lowBleed, fixture.bleed, 5);
        ItemStack lowBleedBook = book(3, 100, 100);
        assertEquals(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE,
                service.apply(lowBleedBook, lowBleed, lowBleed).outcome());
        assertEquals(0, calls.get());
        assertEquals(1, noBleedBook.getCount());
        assertEquals(1, lowBleedBook.getCount());
        assertEquals(5, level(lowBleed, fixture.bleed));
    }

    @Test void successfulConversionReplacesOrdinaryAtTheBooksLevel() {
        var fixture = fixture();
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        set(axe, fixture.bleed, 6);
        ItemStack book = book(3, 100, 100);
        var result = service(fixture, () -> failRoll()).apply(book, axe, axe);
        assertEquals(CosmicBookApplicationResult.Outcome.SUCCESS, result.outcome());
        assertEquals(0, level(axe, fixture.bleed));
        assertEquals(3, level(axe, fixture.deepBleed));
        assertEquals(1, result.slotsUsed());
        assertEquals(1, new CustomEnchantCapacityService().used(axe));
        assertTrue(book.isEmpty());
    }

    @Test void failedConversionsPreserveOrdinaryUnlessDestructionActuallyWins() {
        var fixture = fixture();
        ItemStack survived = maxOrdinary(fixture);
        ItemStack survivedBook = book(2, 1, 1);
        int[] survivedRolls = {100, 100};
        var survivedIndex = new AtomicInteger();
        assertEquals(CosmicBookApplicationResult.Outcome.FAILED_SURVIVED,
                service(fixture, () -> survivedRolls[survivedIndex.getAndIncrement()])
                        .apply(survivedBook, survived, survived).outcome());
        assertEquals(6, level(survived, fixture.bleed));
        assertEquals(0, level(survived, fixture.deepBleed));
        assertTrue(survivedBook.isEmpty());

        ItemStack destroyed = maxOrdinary(fixture);
        assertEquals(CosmicBookApplicationResult.Outcome.FAILED_DESTROYED,
                service(fixture, () -> 100).apply(book(2, 1, 100), destroyed, destroyed).outcome());
        assertTrue(destroyed.isEmpty());

        ItemStack protectedItem = maxOrdinary(fixture);
        var protection = new WhiteScrollProtectionService();
        assertTrue(protection.apply(protectedItem));
        var protectedService = new CosmicBookApplicationService(fixture.registry,
                new CustomEnchantCapacityService(), protection, () -> 100);
        assertEquals(CosmicBookApplicationResult.Outcome.FAILED_PROTECTED,
                protectedService.apply(book(2, 1, 100), protectedItem, protectedItem).outcome());
        assertFalse(protectedItem.isEmpty());
        assertFalse(protection.isProtected(protectedItem));
        assertEquals(6, level(protectedItem, fixture.bleed));
    }

    @Test void existingHeroicUsesNormalHigherEqualAndLowerRules() {
        var fixture = fixture();
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        set(axe, fixture.deepBleed, 2);
        assertEquals(CosmicBookApplicationResult.Outcome.SUCCESS,
                service(fixture, () -> failRoll()).apply(book(4, 100, 100), axe, axe).outcome());
        assertEquals(4, level(axe, fixture.deepBleed));

        ItemStack equal = new ItemStack(Items.DIAMOND_AXE);
        set(equal, fixture.deepBleed, 2);
        assertEquals(CosmicBookApplicationResult.Outcome.SUCCESS,
                service(fixture, () -> failRoll()).apply(book(2, 100, 100), equal, equal).outcome());
        assertEquals(3, level(equal, fixture.deepBleed));

        var calls = new AtomicInteger();
        ItemStack lowerBook = book(1, 100, 100);
        assertEquals(CosmicBookApplicationResult.Outcome.REJECTED_EXISTING_LEVEL,
                service(fixture, () -> { calls.incrementAndGet(); return 1; })
                        .apply(lowerBook, equal, equal).outcome());
        assertEquals(0, calls.get());
        assertEquals(1, lowerBook.getCount());
    }

    @Test void ordinaryCounterpartCannotBeReintroduced() {
        var fixture = fixture();
        ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
        set(axe, fixture.deepBleed, 4);
        ItemStack ordinary = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        ordinary.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(1,
                ModEnchantments.BLEED.identifier(), 6, 100, 100));
        assertEquals(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_COUNTERPART,
                service(fixture, () -> failRoll()).apply(ordinary, axe, axe).outcome());
        assertEquals(4, level(axe, fixture.deepBleed));
        assertEquals(0, level(axe, fixture.bleed));
        assertEquals(1, ordinary.getCount());
    }

    private static CosmicBookApplicationService service(Fixture fixture, java.util.function.IntSupplier rolls) {
        return new CosmicBookApplicationService(fixture.registry, new CustomEnchantCapacityService(),
                new WhiteScrollProtectionService(), rolls);
    }
    private static ItemStack maxOrdinary(Fixture fixture) {
        ItemStack stack = new ItemStack(Items.DIAMOND_AXE);
        set(stack, fixture.bleed, 6);
        return stack;
    }
    private static ItemStack book(int level, int success, int destroy) {
        ItemStack stack = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(1,
                ModEnchantments.DEEP_BLEED.identifier(), level, success, destroy));
        return stack;
    }
    private static void set(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }
    private static int level(ItemStack stack, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
    }
    private static int failRoll() { throw new AssertionError("Deterministic 100% success must not consume RNG"); }
    private static Fixture fixture() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        var supported = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_AXE));
        var bleed = registry.register(ModEnchantments.BLEED, enchantment("Bleed", supported, 6), RegistrationInfo.BUILT_IN);
        var deep = registry.register(ModEnchantments.DEEP_BLEED,
                enchantment("Deep Bleed", supported, 6), RegistrationInfo.BUILT_IN);
        registry.freeze();
        return new Fixture(registry, bleed, deep);
    }
    private static Enchantment enchantment(String name, HolderSet<net.minecraft.world.item.Item> supported, int max) {
        var definition = Enchantment.definition(supported, 1, max, Enchantment.constantCost(1),
                Enchantment.constantCost(1), 1, EquipmentSlotGroup.MAINHAND);
        return new Enchantment(Component.literal(name), definition, HolderSet.empty(), DataComponentMap.EMPTY);
    }
    private record Fixture(MappedRegistry<Enchantment> registry, Holder<Enchantment> bleed,
            Holder<Enchantment> deepBleed) {}
}
