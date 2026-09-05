package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import com.cosmicpve.instance.InstanceBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InventorGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOADOUTS =
            FUNCTIONS.register("inventor_loadouts", ignored -> InventorGameTests::loadouts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CAVE_STARTUP_CLEANUP =
            FUNCTIONS.register("cave_startup_cleanup", ignored -> InventorGameTests::caveStartupCleanup);
    private InventorGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(InventorGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("inventor_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("inventor_loadouts"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("inventor_loadouts")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("cave_startup_cleanup"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cave_startup_cleanup")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void loadouts(GameTestHelper helper) {
        var level = helper.getLevel(); var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var boss = ModEntities.INVENTOR.get().create(level, EntitySpawnReason.EVENT);
        helper.assertTrue(boss != null, "Inventor entity must construct");
        helper.assertTrue(boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                        == com.cosmicpve.entity.inventor.InventorEntity.ATTACK_DAMAGE,
                "Inventor must use the configured 3.5 base attack damage");
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
        helper.assertTrue(player.healingPotions().size() == 2
                        && player.healingPotions().stream().allMatch(stack -> stack.is(Items.SPLASH_POTION)
                                && stack.getCount() == 4
                                && stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
                                        .is(net.minecraft.world.item.alchemy.Potions.HEALING)),
                "Inventor loadout must contain two stacks of four Healing I splash potions");
        helper.assertTrue(player.bread().is(Items.BREAD)
                        && player.bread().getCount() == TrialRoomLoadoutService.INVENTOR_BREAD
                        && TrialRoomLoadoutService.INVENTOR_BREAD_SLOT == 8,
                "Inventor loadout must reserve 16 Bread for Hotbar Slot 9");
        helper.succeed();
    }

    private static void caveStartupCleanup(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos min = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos max = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos inside = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos outside = helper.absolutePos(new BlockPos(6, 2, 2));
        var answer = new CaveDivingService.PotAnswer(List.of(Items.ANGLER_POTTERY_SHERD,
                Items.ARCHER_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD),
                Direction.NORTH);
        var attempt = new CaveDivingService.Attempt(UUID.randomUUID(), new InstanceBounds(min, max), inside,
                inside, answer, Map.of(), new LinkedHashSet<>(), false);
        var startupDrop = new ItemEntity(level, inside.getX() + 0.5D, inside.getY() + 0.5D,
                inside.getZ() + 0.5D, new ItemStack(Items.BRICK));
        var outsideDrop = new ItemEntity(level, outside.getX() + 0.5D, outside.getY() + 0.5D,
                outside.getZ() + 0.5D, new ItemStack(Items.BRICK));
        level.addFreshEntity(startupDrop);
        level.addFreshEntity(outsideDrop);
        level.setBlock(inside.above(), Blocks.DECORATED_POT.defaultBlockState(), 3);
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 3));

        helper.assertTrue(CaveDivingService.cleanupStartupItems(level, attempt) == 1,
                "Startup cleanup must remove exactly the loose item inside the room bounds");
        helper.assertTrue(startupDrop.isRemoved() && !outsideDrop.isRemoved(),
                "Startup cleanup must be bounded to the active Cave Diving room");
        helper.assertTrue(!zombie.isRemoved() && level.getBlockState(inside.above()).is(Blocks.DECORATED_POT)
                        && level.getBlockEntity(inside.above()) != null,
                "Startup cleanup must preserve living entities, blocks, and block entities");

        var laterDrop = new ItemEntity(level, inside.getX() + 0.5D, inside.getY() + 0.5D,
                inside.getZ() + 0.5D, new ItemStack(Items.ANGLER_POTTERY_SHERD));
        level.addFreshEntity(laterDrop);
        helper.assertTrue(CaveDivingService.cleanupStartupItems(level, attempt) == 0 && !laterDrop.isRemoved(),
                "The one-shot startup cleanup must never vacuum later puzzle drops");
        helper.succeed();
    }

    private static int level(ItemStack stack, net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry,
            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(key), stack);
    }
}
