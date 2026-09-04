package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public final class InventorGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOADOUTS =
            FUNCTIONS.register("inventor_loadouts", ignored -> InventorGameTests::loadouts);
    private InventorGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(InventorGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("inventor_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("inventor_loadouts"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("inventor_loadouts")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void loadouts(GameTestHelper helper) {
        var level = helper.getLevel(); var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var boss = ModEntities.INVENTOR.get().create(level, EntitySpawnReason.EVENT);
        helper.assertTrue(boss != null, "Inventor entity must construct");
        InventorService.equipBoss(boss, registry, 4);
        helper.assertTrue(boss.getMainHandItem().is(Items.NETHERITE_AXE), "Four-player Inventor must use Netherite Axe");
        helper.assertTrue(level(boss.getMainHandItem(), registry, ModEnchantments.SOUL_SIPHON) == 4
                && level(boss.getMainHandItem(), registry, ModEnchantments.SOUL_TETHER) == 3
                && level(boss.getMainHandItem(), registry, ModEnchantments.DEEP_BLEED) == 6
                && level(boss.getMainHandItem(), registry, ModEnchantments.INSANITY) == 8
                && level(boss.getMainHandItem(), registry, ModEnchantments.RAGE) == 6
                && level(boss.getMainHandItem(), registry, ModEnchantments.BLESSED) == 4
                && level(boss.getMainHandItem(), registry, ModEnchantments.PUMMEL) == 3,
                "Inventor axe enchantments must be exact");
        CustomEnchantMetadata meta = boss.getMainHandItem().get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        helper.assertTrue(meta != null && meta.orbUpgrades() == 5
                && boss.getMainHandItem().has(ModDataComponents.WEAPON_SKIN.get()), "Inventor axe needs five Orbs and Stormbringer");
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack piece = boss.getItemBySlot(slot);
            helper.assertTrue(piece.has(ModDataComponents.HEROIC.get())
                    && piece.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(ArmorSetIds.ENGINEER),
                    "Every Inventor armor piece must be Heroic Engineer");
        }

        var player = TrialRoomLoadoutService.inventorLoadout(level.registryAccess());
        helper.assertTrue(player.weapon().has(ModDataComponents.ADMIN_ABUSE_REWARD.get())
                && player.weapon().has(ModDataComponents.WEAPON_SKIN.get()), "Player must receive skinned Ashoka");
        helper.assertTrue(player.helmet().has(ModDataComponents.MASK_LOADOUT.get()),
                "Ghostly Veil must carry the Purge/Scarecrow Multi-Mask");
        helper.assertTrue(player.leggings().has(ModDataComponents.HEROIC.get())
                && player.leggings().get(ModDataComponents.CUSTOM_ENCHANT_META.get()).transmogSorted(),
                "Player leggings must be Heroic and Transmogged");
        helper.succeed();
    }

    private static int level(ItemStack stack, net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry,
            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(key), stack);
    }
}
