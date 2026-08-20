package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.BlackScrollData;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class BlackScrollExtractionTest {
    @Test
    void typedDataAndCodecEnforceInclusiveReturnedSuccessRange() {
        assertEquals(1, new BlackScrollData(1, 1).returnedSuccessRate());
        assertEquals(100, new BlackScrollData(1, 100).returnedSuccessRate());
        assertThrows(IllegalArgumentException.class, () -> new BlackScrollData(1, 0));
        var malformed = com.google.gson.JsonParser.parseString("{\"returned_success_rate\":101}");
        assertTrue(BlackScrollData.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
        assertEquals(1, BlackScrollItem.MAX_STACK_SIZE);
    }

    @Test
    void oneActualEnchantExtractsWithoutSelectionRollAndReturnsExactBookData() {
        var fixture = fixture();
        var target = new ItemStack(Items.DIAMOND_SWORD);
        addEnchant(target, fixture.execute(), 5);
        var selectionCalls = new AtomicInteger();
        var destroyCalls = new AtomicInteger();
        var service = new BlackScrollExtractionService(fixture.registry(), bound -> {
            selectionCalls.incrementAndGet();
            return 0;
        }, () -> {
            destroyCalls.incrementAndGet();
            return 37;
        });
        var scroll = scroll(100);

        var result = service.apply(scroll, target, target);

        assertEquals(BlackScrollExtractionResult.Outcome.SUCCESS, result.outcome());
        assertEquals(ModEnchantments.EXECUTE.identifier(), result.enchantmentId());
        assertEquals(5, result.level());
        assertEquals(100, result.returnedSuccessRate());
        assertEquals(37, result.returnedDestroyRate());
        assertEquals(0, selectionCalls.get());
        assertEquals(1, destroyCalls.get());
        assertEquals(0, EnchantmentHelper.getEnchantmentsForCrafting(target).getLevel(fixture.execute()));
        assertTrue(scroll.isEmpty());
        var bookData = result.returnedBook().get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        assertNotNull(bookData);
        assertEquals(ModEnchantments.EXECUTE.identifier(), bookData.enchantmentId());
        assertEquals(5, bookData.level());
        assertEquals(100, bookData.successRate());
        assertEquals(37, bookData.destroyRate());
    }

    @Test
    void deterministicUniformCandidateIndexRemovesOnlySelectedAndPreservesAllOtherData() {
        var fixture = fixture();
        var target = new ItemStack(Items.DIAMOND_SWORD);
        addEnchant(target, fixture.execute(), 5);
        addEnchant(target, fixture.doublestrike(), 3);
        addEnchant(target, fixture.vanilla(), 4);
        var metadata = new CustomEnchantMetadata(
                CustomEnchantMetadata.CURRENT_DATA_VERSION, 5, 2, true, true);
        target.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), metadata);
        var identity = new ArmorSetIdentity(1, CosmicPVE.id("phantom"), Component.literal("Phantom"),
                0xFF6969, List.of(Component.literal("bonus")));
        target.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        target.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved Sword"));
        target.setDamageValue(17);
        var service = new BlackScrollExtractionService(fixture.registry(), bound -> {
            assertEquals(2, bound);
            return 0; // Deterministic ID order is doublestrike, then execute.
        }, () -> 88);

        var result = service.apply(scroll(75), target, target);

        assertEquals(ModEnchantments.DOUBLESTRIKE.identifier(), result.enchantmentId());
        var enchants = EnchantmentHelper.getEnchantmentsForCrafting(target);
        assertEquals(0, enchants.getLevel(fixture.doublestrike()));
        assertEquals(5, enchants.getLevel(fixture.execute()));
        assertEquals(4, enchants.getLevel(fixture.vanilla()));
        assertEquals(metadata, target.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
        assertEquals(identity, target.get(ModDataComponents.ARMOR_SET_ID.get()));
        assertEquals("Preserved Sword", target.getHoverName().getString());
        assertEquals(17, target.getDamageValue());
    }

    @Test
    void virtualOnlyOrEmptyTargetsRejectBeforeConsumptionOrRandomness() {
        var fixture = fixture();
        var target = new ItemStack(Items.DIAMOND_SWORD);
        var before = target.copy();
        var scroll = scroll(40);
        var selectionCalls = new AtomicInteger();
        var destroyCalls = new AtomicInteger();
        var service = new BlackScrollExtractionService(fixture.registry(), bound -> {
            selectionCalls.incrementAndGet(); return 0;
        }, () -> {
            destroyCalls.incrementAndGet(); return 50;
        });
        var virtual = new EffectiveEnchantmentsResolver().resolveSources(List.of(), List.of(
                new VirtualEnchantmentGrant(ModEnchantments.DOUBLESTRIKE.identifier(), 3, CosmicPVE.id("skin"))));
        assertEquals(3, virtual.level(ModEnchantments.DOUBLESTRIKE.identifier()));

        var result = service.apply(scroll, target, target);

        assertEquals(BlackScrollExtractionResult.Outcome.REJECTED_NO_ELIGIBLE_ENCHANTMENTS, result.outcome());
        assertEquals(0, selectionCalls.get());
        assertEquals(0, destroyCalls.get());
        assertEquals(1, scroll.getCount());
        assertTrue(ItemStack.matches(before, target));
    }

    @Test
    void staleTargetRejectsAndSuccessfulOutputReplacesCursorWithoutInventorySpace() {
        var fixture = fixture();
        var expected = new ItemStack(Items.DIAMOND_SWORD);
        var current = new ItemStack(Items.DIAMOND_SWORD);
        addEnchant(current, fixture.execute(), 1);
        var calls = new AtomicInteger();
        var service = new BlackScrollExtractionService(fixture.registry(), bound -> {
            calls.incrementAndGet(); return 0;
        }, () -> {
            calls.incrementAndGet(); return 12;
        });
        var staleScroll = scroll(75);
        assertEquals(BlackScrollExtractionResult.Outcome.STALE_TARGET,
                service.apply(staleScroll, expected, current).outcome());
        assertEquals(0, calls.get());
        assertEquals(1, staleScroll.getCount());

        var appliedScroll = scroll(75);
        var success = service.apply(appliedScroll, current, current);
        ItemStack cursor = BlackScrollCursorOutput.afterApplication(appliedScroll, success);
        assertTrue(cursor.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get()));
        assertSame(success.returnedBook(), cursor);
        assertEquals(1, calls.get()); // one candidate skips selection; only Destroy Rate rolls
    }

    private static ItemStack scroll(int successRate) {
        var stack = new ItemStack(ModItems.BLACK_SCROLL.get());
        stack.set(ModDataComponents.BLACK_SCROLL.get(), new BlackScrollData(1, successRate));
        return stack;
    }

    private static void addEnchant(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }

    private static Fixture fixture() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        var execute = registry.register(ModEnchantments.EXECUTE, enchantment("execute", 5), RegistrationInfo.BUILT_IN);
        var doublestrike = registry.register(
                ModEnchantments.DOUBLESTRIKE, enchantment("doublestrike", 3), RegistrationInfo.BUILT_IN);
        var vanillaKey = ModEnchantments.createKey("test_vanilla");
        var vanilla = registry.register(vanillaKey, enchantment("test_vanilla", 5), RegistrationInfo.BUILT_IN);
        registry.freeze();
        return new Fixture(registry, execute, doublestrike, vanilla);
    }

    private static Enchantment enchantment(String name, int maximumLevel) {
        var supported = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_SWORD));
        var definition = Enchantment.definition(supported, 1, maximumLevel,
                Enchantment.constantCost(1), Enchantment.constantCost(1), 1, EquipmentSlotGroup.MAINHAND);
        return new Enchantment(Component.literal(name), definition, HolderSet.empty(), DataComponentMap.EMPTY);
    }

    private record Fixture(MappedRegistry<Enchantment> registry, Holder<Enchantment> execute,
            Holder<Enchantment> doublestrike, Holder<Enchantment> vanilla) {}
}
