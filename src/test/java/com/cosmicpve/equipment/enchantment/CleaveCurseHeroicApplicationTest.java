package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class CleaveCurseHeroicApplicationTest {
    @Test void newHeroicsRequireMaximumOrdinaryConvertInOneSlotAndThenUpgradeNormally() {
        var fixture = fixture();
        verifyPair(fixture, Items.DIAMOND_AXE, ModEnchantments.CLEAVE, ModEnchantments.MIGHTY_CLEAVE, 8);
        verifyPair(fixture, Items.IRON_CHESTPLATE, ModEnchantments.CURSE, ModEnchantments.FORBIDDEN_CURSE, 5);
        verifyPair(fixture, Items.IRON_CHESTPLATE, ModEnchantments.OVERLOAD, ModEnchantments.GODLY_OVERLOAD, 3);
    }

    private static void verifyPair(Fixture fixture, Item item, ResourceKey<Enchantment> ordinaryKey,
            ResourceKey<Enchantment> heroicKey, int ordinaryMax) {
        Holder<Enchantment> ordinary = fixture.registry.getOrThrow(ordinaryKey);
        Holder<Enchantment> heroic = fixture.registry.getOrThrow(heroicKey);
        var service = new CosmicBookApplicationService(fixture.registry, new CustomEnchantCapacityService(),
                new WhiteScrollProtectionService(), () -> { throw new AssertionError("100% success must not roll"); });

        ItemStack tooLow = new ItemStack(item);
        set(tooLow, ordinary, ordinaryMax - 1);
        assertEquals(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE,
                service.apply(book(heroicKey.identifier(), 1), tooLow, tooLow).outcome());

        ItemStack converted = new ItemStack(item);
        set(converted, ordinary, ordinaryMax);
        assertEquals(CosmicBookApplicationResult.Outcome.SUCCESS,
                service.apply(book(heroicKey.identifier(), 1), converted, converted).outcome());
        assertEquals(0, level(converted, ordinary));
        assertEquals(1, level(converted, heroic));
        assertEquals(1, new CustomEnchantCapacityService().used(converted));

        assertEquals(CosmicBookApplicationResult.Outcome.SUCCESS,
                service.apply(book(heroicKey.identifier(), 1), converted, converted).outcome());
        assertEquals(2, level(converted, heroic));
        assertEquals(1, new CustomEnchantCapacityService().used(converted));
    }

    private static ItemStack book(Identifier id, int level) {
        ItemStack book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        book.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(),
                new CosmicEnchantmentBookData(1, id, level, 100, 100));
        return book;
    }

    private static void set(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }

    private static int level(ItemStack stack, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
    }

    private static Fixture fixture() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        register(registry, ModEnchantments.CLEAVE, Items.DIAMOND_AXE, 8);
        register(registry, ModEnchantments.MIGHTY_CLEAVE, Items.DIAMOND_AXE, 8);
        register(registry, ModEnchantments.CURSE, Items.IRON_CHESTPLATE, 5);
        register(registry, ModEnchantments.FORBIDDEN_CURSE, Items.IRON_CHESTPLATE, 5);
        register(registry, ModEnchantments.OVERLOAD, Items.IRON_CHESTPLATE, 3);
        register(registry, ModEnchantments.GODLY_OVERLOAD, Items.IRON_CHESTPLATE, 3);
        registry.freeze();
        return new Fixture(registry);
    }

    private static void register(MappedRegistry<Enchantment> registry, ResourceKey<Enchantment> key,
            Item item, int maxLevel) {
        HolderSet<Item> supported = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item));
        var definition = Enchantment.definition(supported, 1, maxLevel, Enchantment.constantCost(1),
                Enchantment.constantCost(1), 1, item == Items.IRON_CHESTPLATE
                        ? EquipmentSlotGroup.CHEST : EquipmentSlotGroup.MAINHAND);
        registry.register(key, new Enchantment(Component.literal(key.identifier().getPath()), definition,
                HolderSet.empty(), DataComponentMap.EMPTY), RegistrationInfo.BUILT_IN);
    }

    private record Fixture(MappedRegistry<Enchantment> registry) {}
}
