package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry proof for all five definitions and both expanded Table matrices. */
public final class CleaveCurseGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static {
        FUNCTIONS.register("cleave_curse_registry_tables", ignored -> CleaveCurseGameTests::verify);
        FUNCTIONS.register("boss_tank_trap_loaded", ignored -> CleaveCurseGameTests::verifyMilestone);
    }
    private CleaveCurseGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(CleaveCurseGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("cleave_curse_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("cleave_curse_registry_tables"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cleave_curse_registry_tables")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("boss_tank_trap_loaded"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("boss_tank_trap_loaded")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void verify(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        assertLevel(helper, registry, ModEnchantments.CLEAVE, 8);
        assertLevel(helper, registry, ModEnchantments.SOLITUDE, 3);
        assertLevel(helper, registry, ModEnchantments.CURSE, 5);
        assertLevel(helper, registry, ModEnchantments.MIGHTY_CLEAVE, 8);
        assertLevel(helper, registry, ModEnchantments.FORBIDDEN_CURSE, 5);

        var ordinary = new CosmicEnchantmentTableRewards();
        helper.assertTrue(CosmicEnchantmentTableRewards.POOL.size() == 18,
                "ordinary Table must contain exactly the 18 starred enchantments");
        for (var key : CosmicEnchantmentTableRewards.POOL) {
            var successes = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(key.identifier())
                    .orElseThrow().tier() == com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier.MASTERY
                    ? CosmicEnchantmentTableRewards.MASTERY_SUCCESS : CosmicEnchantmentTableRewards.ORDINARY_SUCCESS;
            for (int success : successes) {
                CosmicEnchantmentBookData data = ordinary.create(registry, key, success, RandomSource.create(1))
                        .get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
                helper.assertTrue(data != null && data.level() == registry.getOrThrow(key).value().getMaxLevel()
                        && data.successRate() == success, "ordinary Table output must be max-level with canonical rate");
            }
        }

        var heroic = new HeroicCosmicEnchantmentTableRewards();
        helper.assertTrue(HeroicCosmicEnchantmentTableRewards.POOL.size() == 13
                && HeroicCosmicEnchantmentTableRewards.ENTRY_COUNT == 39, "Heroic Table must contain 13x3 outcomes");
        for (var id : HeroicCosmicEnchantmentTableRewards.POOL) for (int success : HeroicCosmicEnchantmentTableRewards.SUCCESS) {
            CosmicEnchantmentBookData data = heroic.create(registry, id, success, RandomSource.create(2))
                    .get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            helper.assertTrue(data != null && data.successRate() == success,
                    "Heroic Table output must contain every canonical success combination");
        }
        helper.succeed();
    }

    private static void assertLevel(GameTestHelper helper, net.minecraft.core.Registry<Enchantment> registry,
            ResourceKey<Enchantment> key, int max) {
        helper.assertTrue(registry.getOrThrow(key).value().getMaxLevel() == max,
                key.identifier() + " must decode at max level " + max);
    }

    private static void verifyMilestone(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        assertLevel(helper, registry, ModEnchantments.BOSS_SLAYER, 3);
        assertLevel(helper, registry, ModEnchantments.TANK, 4);
        assertLevel(helper, registry, ModEnchantments.TRAP, 3);
        assertLevel(helper, registry, ModEnchantments.TITAN_TRAP, 3);
        helper.assertTrue(HeroicCosmicEnchantmentTableRewards.POOL.contains(
                ModEnchantments.TITAN_TRAP.identifier()), "Titan Trap must enter the Heroic Table");
        var content = com.cosmicpve.content.CosmicContent.repository();
        var trap = content.requireStackDefinition(TrapBehavior.STACK_ID);
        helper.assertTrue(trap.polarity() == com.cosmicpve.content.definition.stack.StackPolarity.NEGATIVE
                && trap.maximumStacks() == 1 && trap.cleansable(), "Trap must be a cleansable single negative stack");
        helper.assertTrue(content.findMaskDefinition(CosmicPVE.id("bandit")).isPresent(),
                "Bandit Mask must load from the content registry");

        var attacker = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var boss = helper.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM,
                new net.minecraft.core.BlockPos(7, 2, 7));
        var effective = new com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver();
        var bossGrant = effective.resolveSources(java.util.List.of(
                new com.cosmicpve.equipment.enchantment.ActualEnchantmentGrant(
                        ModEnchantments.BOSS_SLAYER.identifier(), 3, CosmicPVE.id("test_weapon"))), java.util.List.of());
        var slayer = new BossSlayerBehavior();
        boss.setHealth(60);
        helper.assertTrue(slayer.resolve(context(attacker, boss, bossGrant,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD),
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY)).getFirst().bonus() == .09,
                "Boss Slayer must qualify at exactly 3x pre-hit health");
        boss.setHealth(59);
        helper.assertTrue(slayer.resolve(context(attacker, boss, bossGrant,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD),
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY)).isEmpty(), "Below 3x must fail");
        boss.setHealth(80);
        helper.assertTrue(slayer.resolve(context(attacker, boss, bossGrant,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK),
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY)).isEmpty(), "Wrong weapon must fail");
        helper.assertTrue(slayer.resolve(context(attacker, boss, bossGrant,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_AXE),
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.TRUE)).isEmpty(), "True damage must not scale");

        var identity = com.cosmicpve.data.component.ArmorSetIdentity.from(content.requireArmorSetDefinition(
                com.cosmicpve.equipment.armor.ArmorSetIds.DRAGONSLAYER));
        var head = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET);
        var chest = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE);
        var legs = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_LEGGINGS);
        var feet = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_BOOTS);
        head.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        chest.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        legs.set(ModDataComponents.OMNI_ARMOR.get(), true);
        feet.set(ModDataComponents.OMNI_ARMOR.get(), true);
        attacker.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, head);
        attacker.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chest);
        attacker.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, legs);
        attacker.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, feet);
        var tankHead = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET);
        var tankChest = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE);
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(tankHead,
                mutable -> mutable.set(registry.getOrThrow(ModEnchantments.TANK), 4));
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(tankChest,
                mutable -> mutable.set(registry.getOrThrow(ModEnchantments.TANK), 4));
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, tankHead);
        boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, tankChest);
        var tank = new TankBehavior(new com.cosmicpve.equipment.armor.ArmorSetResolver(content), effective);
        helper.assertTrue(tank.resolveIncoming(context(attacker, boss,
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                net.minecraft.world.item.ItemStack.EMPTY, com.cosmicpve.combat.api.AttackCategory.PROJECTILE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY)).getFirst().multiplier() == .92,
                "Root attacker with active Omni-resolved set must trigger capped projectile Tank reduction");
        head.remove(ModDataComponents.ARMOR_SET_ID.get());
        helper.assertTrue(tank.resolveIncoming(context(attacker, boss,
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                net.minecraft.world.item.ItemStack.EMPTY, com.cosmicpve.combat.api.AttackCategory.PROJECTILE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY)).isEmpty(), "Inactive set must not trigger Tank");
        var looseBandit = com.cosmicpve.equipment.mask.MaskItemFactory.create(CosmicPVE.id("bandit"));
        var maskApplication = new com.cosmicpve.equipment.mask.MaskApplicationService(content);
        helper.assertTrue(maskApplication.apply(looseBandit, head, looseBandit, head)
                == com.cosmicpve.equipment.mask.MaskApplicationService.ApplyOutcome.SUCCESS,
                "Bandit must attach through the ordinary helmet Mask service");
        var activities = new com.cosmicpve.activity.ActivityContextService();
        var masks = new com.cosmicpve.equipment.mask.MaskCombatResolver(
                new com.cosmicpve.equipment.mask.MaskResolver(content), activities);
        var maskContext = context(attacker, boss,
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                net.minecraft.world.item.ItemStack.EMPTY, com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY);
        helper.assertTrue(masks.resolve(maskContext).isEmpty(), "Bandit is inert outside Adventures");
        activities.set(attacker.getUUID(), com.cosmicpve.activity.ActivityType.ADVENTURE);
        helper.assertTrue(masks.resolve(maskContext).getFirst().bonus() == .08,
                "Bandit adds exactly eight percent in a Mask-permitted Adventure");
        activities.clear(attacker.getUUID());
        maskApplication.remove(head, head, true);

        var target = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE,
                new net.minecraft.core.BlockPos(3, 2, 3));
        var stacks = new com.cosmicpve.combat.stack.CombatStackService(content);
        var roots = new SnareRootService();
        long tick = helper.getLevel().getServer().getTickCount();
        var titanGrant = effective.resolveSources(java.util.List.of(), java.util.List.of(
                new com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant(
                        ModEnchantments.TITAN_TRAP.identifier(), 3,
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.FIREWORK_ROCKET)));
        var titan = new TitanTrapBehavior(stacks);
        var titanContext = context(attacker, target, titanGrant,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_AXE),
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY);
        helper.assertTrue(titan.resolve(titanContext).isEmpty(), "First hit must not receive Titan passive bonus");
        stacks.addStack(target, TrapBehavior.STACK_ID, 1,
                com.cosmicpve.combat.stack.StackApplication.unattributed(), tick, 15);
        helper.assertTrue(titan.resolve(titanContext).getFirst().bonus() == .12,
                "Virtual Titan III Axe must receive the already-trapped ordinary bonus");
        roots.applyTrap(target);
        double x = target.getX(), z = target.getZ();
        target.setDeltaMovement(1, .5, 1);
        target.setPos(x + .5, target.getY(), z + .5);
        roots.tickTrap(target, stacks.count(target, TrapBehavior.STACK_ID, tick) > 0);
        helper.assertTrue(target.getX() == x && target.getZ() == z
                && target.getDeltaMovement().x == 0 && target.getDeltaMovement().z == 0,
                "Trap must deny locomotion and knockback while active");
        roots.markTeleport(target.getUUID());
        target.setPos(x + .5, target.getY(), z + .5);
        roots.tickTrap(target, true);
        helper.assertTrue(target.getX() == x + .5 && target.getZ() == z + .5,
                "Authoritative teleport must reset even a short Trap anchor");
        stacks.cleanseAll(target, com.cosmicpve.content.definition.stack.StackPolarity.NEGATIVE, tick);
        roots.tickTrap(target, stacks.count(target, TrapBehavior.STACK_ID, tick) > 0);
        helper.assertTrue(!roots.hasTrapAnchor(target.getUUID()), "Cleansing must release Trap's movement anchor");
        helper.succeed();
    }

    private static com.cosmicpve.combat.api.CombatContext context(net.minecraft.world.entity.LivingEntity attacker,
            net.minecraft.world.entity.LivingEntity target,
            com.cosmicpve.equipment.enchantment.EffectiveEnchantments enchants,
            net.minecraft.world.item.ItemStack weapon, com.cosmicpve.combat.api.AttackCategory category,
            com.cosmicpve.combat.api.DamageChannel channel) {
        return new com.cosmicpve.combat.api.CombatContext(attacker, attacker, attacker, target,
                java.util.Optional.empty(), target.damageSources().mobAttack(attacker), category, channel,
                java.util.Set.of(), new com.cosmicpve.combat.api.WeaponSnapshot(weapon), enchants, 1,
                java.util.OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
    }
}
