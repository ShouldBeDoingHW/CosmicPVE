package com.cosmicpve.tinkerer;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.data.component.WeaponSkinIdentity;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class GearSalvageServiceTest {
    @Test void canonicalMixedBootsProduceExactlyOneTypedBottleWorth2275Xp() {
        var fixture = fixture();
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        enchant(boots, fixture.protection(), 4);
        enchant(boots, fixture.unbreaking(), 3);
        enchant(boots, fixture.gears(), 2);
        enchant(boots, fixture.molten(), 4);
        var input = new SimpleContainer(3);
        ItemStack book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        book.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                1, ModEnchantments.MOLTEN.identifier(), 1, 50, 50));
        input.setItem(1, book);
        input.setItem(2, boots);

        var result = new TinkererSalvageService().confirm(input, 1, 3);

        assertTrue(result.succeeded());
        assertEquals(1, result.consumedGear());
        assertEquals(1, result.consumedBooks());
        assertFalse(result.dust().isEmpty());
        assertEquals(1, result.xpBottles().size());
        ItemStack bottle = result.xpBottles().getFirst();
        assertTrue(bottle.is(ModItems.SALVAGED_XP_BOTTLE.get()));
        assertEquals(2275, bottle.get(ModDataComponents.STORED_XP_BOTTLE.get()).storedXp());
        assertTrue(input.getItem(1).isEmpty());
        assertTrue(input.getItem(2).isEmpty());
        assertFalse(bottle.getItem() instanceof net.minecraft.world.item.ProjectileItem);
    }

    @Test void exactTierConstantsAndMasteryRemainPerLevel() {
        assertEquals(25, GearSalvageService.VANILLA_XP_PER_LEVEL);
        assertEquals(75, GearSalvageService.perLevel(CosmicEnchantmentTier.SIMPLE));
        assertEquals(125, GearSalvageService.perLevel(CosmicEnchantmentTier.UNIQUE));
        assertEquals(250, GearSalvageService.perLevel(CosmicEnchantmentTier.ELITE));
        assertEquals(475, GearSalvageService.perLevel(CosmicEnchantmentTier.ULTIMATE));
        assertEquals(800, GearSalvageService.perLevel(CosmicEnchantmentTier.LEGENDARY));
        assertEquals(2500, GearSalvageService.perLevel(CosmicEnchantmentTier.MASTERY));
        assertEquals(2500, GearSalvageService.HEROIC_XP_PER_LEVEL);
        assertEquals(7500, GearSalvageService.perLevel(CosmicEnchantmentTier.MASTERY) * 3);
    }

    @Test void attachedMaskOrWeaponSkinRejectsWithoutMutatingActualGear() {
        var fixture = fixture();
        var service = new GearSalvageService();
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        enchant(boots, fixture.gears(), 1);
        assertTrue(service.storedXp(boots).isPresent());
        boots.set(ModDataComponents.MASK_LOADOUT.get(), new MaskLoadout(List.of(CosmicPVE.id("test_mask"))));
        assertTrue(service.storedXp(boots).isEmpty());

        ItemStack bow = new ItemStack(Items.BOW);
        enchant(bow, fixture.protection(), 1);
        assertTrue(service.storedXp(bow).isPresent());
        bow.set(ModDataComponents.WEAPON_SKIN.get(), new WeaponSkinIdentity(1,
                CosmicPVE.id("test_skin"), Optional.empty()));
        assertTrue(service.storedXp(bow).isEmpty());
    }

    @Test void ineligibleToolsAndUnenchantedGearAreRejectedWithoutConsumption() {
        var service = new GearSalvageService();
        assertTrue(service.storedXp(new ItemStack(Items.DIAMOND_PICKAXE)).isEmpty());
        assertTrue(service.storedXp(new ItemStack(Items.DIAMOND_SWORD)).isEmpty());
        var input = new SimpleContainer(2);
        input.setItem(1, new ItemStack(Items.DIAMOND_SWORD));
        assertFalse(new TinkererSalvageService().confirm(input, 1, 2).succeeded());
        assertFalse(input.getItem(1).isEmpty());
    }

    @Test void typedPayloadRejectsZeroAndMalformedCodecData() {
        assertThrows(IllegalArgumentException.class, () -> new com.cosmicpve.data.component.StoredXpBottleData(1, 0));
        var malformed = JsonParser.parseString("{\"stored_xp\":0}");
        assertTrue(com.cosmicpve.data.component.StoredXpBottleData.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
        assertEquals(1, ModItems.SALVAGED_XP_BOTTLE.get().getDefaultMaxStackSize());

        ItemStack first = new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get());
        first.set(ModDataComponents.STORED_XP_BOTTLE.get(),
                new com.cosmicpve.data.component.StoredXpBottleData(1, 125));
        ItemStack copy = first.copy();
        assertEquals(first.get(ModDataComponents.STORED_XP_BOTTLE.get()),
                copy.get(ModDataComponents.STORED_XP_BOTTLE.get()));
        ItemStack second = new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get());
        second.set(ModDataComponents.STORED_XP_BOTTLE.get(),
                new com.cosmicpve.data.component.StoredXpBottleData(1, 250));
        assertFalse(ItemStack.isSameItemSameComponents(first, second));
    }

    @Test void storedXpBottleRedeemsRawPointsOnceWithoutProjectileOrOverflow() {
        ItemStack single = bottle(2275, 1);
        var points = new AtomicInteger();
        assertTrue(SalvagedXpBottleItem.redeem(single, 0, points::addAndGet));
        assertEquals(2275, points.get());
        assertTrue(single.isEmpty());

        ItemStack syntheticMulti = bottle(800, 3);
        assertTrue(SalvagedXpBottleItem.redeem(syntheticMulti, 100, points::addAndGet));
        assertEquals(3075, points.get());
        assertEquals(2, syntheticMulti.getCount());
        assertFalse(syntheticMulti.getItem() instanceof net.minecraft.world.item.ProjectileItem);
        assertEquals(1.5F, SalvagedXpBottleItem.REDEEM_PITCH);
        assertEquals(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, SalvagedXpBottleItem.redeemSound());

        ItemStack missing = new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get());
        assertFalse(SalvagedXpBottleItem.redeem(missing, 0, points::addAndGet));
        assertEquals(1, missing.getCount());
        ItemStack overflow = bottle(10, 1);
        assertFalse(SalvagedXpBottleItem.redeem(overflow, Integer.MAX_VALUE - 5, points::addAndGet));
        assertEquals(1, overflow.getCount());
        assertEquals(3075, points.get());
    }

    @Test void xpBottleSuccessMessageUsesGroupedBoldStandardGreenPresentation() {
        var message = SalvagedXpBottleItem.successMessage(2_275);
        assertEquals("+2,275 XP", message.getString());
        assertTrue(message.getStyle().isBold());
        assertEquals(0x55FF55, message.getStyle().getColor().getValue());
    }

    @Test void insertionFeedbackOnlyCoversSuccessfulPlayerToTinkererMoves() {
        assertEquals(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE,
                TinkererInsertionFeedback.sound());
        assertTrue(TinkererInsertionFeedback.shouldPlay(0, 1, true,
                net.minecraft.world.inventory.ClickType.PICKUP));
        assertTrue(TinkererInsertionFeedback.shouldPlay(0, 1, true,
                net.minecraft.world.inventory.ClickType.QUICK_MOVE));
        assertFalse(TinkererInsertionFeedback.shouldPlay(0, 0, true,
                net.minecraft.world.inventory.ClickType.PICKUP));
        assertFalse(TinkererInsertionFeedback.shouldPlay(1, 0, false,
                net.minecraft.world.inventory.ClickType.PICKUP));
        assertFalse(TinkererInsertionFeedback.shouldPlay(0, 1, false,
                net.minecraft.world.inventory.ClickType.PICKUP));

        ItemStack book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        ItemStack empty = ItemStack.EMPTY;
        assertTrue(TinkererInsertionFeedback.shouldPlay(empty, book, book, true,
                net.minecraft.world.inventory.ClickType.PICKUP));
        assertFalse(TinkererInsertionFeedback.shouldPlay(book, empty, empty, false,
                net.minecraft.world.inventory.ClickType.PICKUP));
        assertFalse(TinkererInsertionFeedback.shouldPlay(empty, empty, book, true,
                net.minecraft.world.inventory.ClickType.PICKUP));
    }

    private static ItemStack bottle(long xp, int count) {
        ItemStack stack = new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get());
        stack.set(ModDataComponents.STORED_XP_BOTTLE.get(),
                new com.cosmicpve.data.component.StoredXpBottleData(1, xp));
        stack.setCount(count);
        return stack;
    }

    private static void enchant(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
    }
    private static Fixture fixture() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        var protection = registry.register(ModEnchantments.createKey("test_protection"), enchantment("Protection", 4), RegistrationInfo.BUILT_IN);
        var unbreaking = registry.register(ModEnchantments.createKey("test_unbreaking"), enchantment("Unbreaking", 3), RegistrationInfo.BUILT_IN);
        var gears = registry.register(ModEnchantments.GEARS, enchantment("Gears", 3), RegistrationInfo.BUILT_IN);
        var molten = registry.register(ModEnchantments.MOLTEN, enchantment("Molten", 4), RegistrationInfo.BUILT_IN);
        registry.freeze();
        return new Fixture(protection, unbreaking, gears, molten);
    }
    private static Enchantment enchantment(String name, int max) {
        var supported = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_BOOTS));
        return new Enchantment(Component.literal(name), Enchantment.definition(supported, 1, max,
                Enchantment.constantCost(1), Enchantment.constantCost(1), 1, EquipmentSlotGroup.ARMOR),
                HolderSet.empty(), DataComponentMap.EMPTY);
    }
    private record Fixture(Holder<Enchantment> protection, Holder<Enchantment> unbreaking,
                           Holder<Enchantment> gears, Holder<Enchantment> molten) {}
}
