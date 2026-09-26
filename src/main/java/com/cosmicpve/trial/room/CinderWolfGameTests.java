package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** In-game registry-backed loadout and spawnable entity smoke test. */
public final class CinderWolfGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOADOUT =
            FUNCTIONS.register("cinder_wolf_loadout", ignored -> CinderWolfGameTests::loadout);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENCOUNTER =
            FUNCTIONS.register("cinder_wolf_encounter", ignored -> CinderWolfGameTests::encounter);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEATH_COFFIN =
            FUNCTIONS.register("death_coffin_child", ignored -> CinderWolfGameTests::deathCoffinChild);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PUP_AGGRO =
            FUNCTIONS.register("cinder_pup_aggro", ignored -> CinderWolfGameTests::pupAggro);
    private CinderWolfGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(CinderWolfGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("cinder_wolf_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("cinder_wolf_loadout"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cinder_wolf_loadout")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("cinder_wolf_encounter"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cinder_wolf_encounter")),
                new TestData<>(environment, CosmicPVE.id("trial/cinderwolf"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("death_coffin_child"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("death_coffin_child")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("cinder_pup_aggro"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("cinder_pup_aggro")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 120, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void loadout(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var gear = TrialRoomLoadoutService.cinderWolfLoadout(helper.getLevel().registryAccess());
        ItemStack[] armor = {gear.helmet(), gear.chestplate(), gear.leggings(), gear.boots()};
        net.minecraft.world.item.Item[] items = {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};
        for (int index = 0; index < 4; index++) {
            helper.assertTrue(armor[index].is(items[index]), "Cinder Wolf armor type " + index);
            helper.assertTrue(level(armor[index], registry.getOrThrow(Enchantments.PROTECTION)) == 2
                    && level(armor[index], registry.getOrThrow(Enchantments.UNBREAKING)) == 3,
                    "Every Cinder Wolf armor piece needs Protection II and Unbreaking III");
        }
        helper.assertTrue(gear.helmet().has(ModDataComponents.HEROIC.get())
                && gear.chestplate().has(ModDataComponents.HEROIC.get()), "Heroic armor state required");
        helper.assertTrue(level(gear.helmet(), registry.getOrThrow(ModEnchantments.ENDER_SHIFT)) == 3
                && level(gear.helmet(), registry.getOrThrow(ModEnchantments.PLANETARY_DEATHBRINGER)) == 3,
                "Helmet Cosmic enchants");
        helper.assertTrue(level(gear.chestplate(), registry.getOrThrow(ModEnchantments.GODLY_OVERLOAD)) == 3
                && level(gear.chestplate(), registry.getOrThrow(ModEnchantments.AEGIS)) == 6,
                "Chest Cosmic enchants");
        helper.assertTrue(level(gear.leggings(), registry.getOrThrow(ModEnchantments.NUTRITION)) == 3
                && level(gear.leggings(), registry.getOrThrow(ModEnchantments.CACTUS)) == 2,
                "Leggings Cosmic enchants");
        helper.assertTrue(level(gear.boots(), registry.getOrThrow(ModEnchantments.GEARS)) == 3,
                "Boots Gears III");
        helper.assertTrue(gear.axe().is(Items.DIAMOND_AXE)
                && level(gear.axe(), registry.getOrThrow(Enchantments.SHARPNESS)) == 5
                && level(gear.axe(), registry.getOrThrow(Enchantments.UNBREAKING)) == 3
                && level(gear.axe(), registry.getOrThrow(ModEnchantments.DEATH_COFFIN)) == 3
                && level(gear.axe(), registry.getOrThrow(ModEnchantments.HEX)) == 5
                && level(gear.axe(), registry.getOrThrow(ModEnchantments.INSANITY)) == 8
                && level(gear.axe(), registry.getOrThrow(ModEnchantments.PUMMEL)) == 3,
                "Diamond Axe enchantments");
        helper.assertTrue(gear.water().is(Items.SPLASH_POTION)
                && gear.goldenApples().is(Items.GOLDEN_APPLE) && gear.goldenApples().getCount() == 5
                && gear.potatoes().is(Items.BAKED_POTATO) && gear.potatoes().getCount() == 32,
                "Water, five golden apples, and food supplies");
        helper.succeed();
    }
    private static int level(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    private static void encounter(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(net.minecraft.core.BlockPos.ZERO);
        var bounds = new com.cosmicpve.instance.InstanceBounds(origin, origin.offset(32, 19, 33));
        var session = com.cosmicpve.trial.TrialSession.joining(java.util.UUID.randomUUID(),
                net.minecraft.resources.Identifier.withDefaultNamespace("overworld"), origin,
                java.util.List.of(), java.util.List.of(bounds)).addParticipant(java.util.UUID.randomUUID());
        var service = new CinderWolfService();
        helper.assertTrue(level.getBlockState(origin.offset(CinderWolfService.BOSS_MARKER_LOCAL))
                .is(net.minecraft.world.level.block.Blocks.STRIPPED_CHERRY_WOOD), "authored boss marker");
        helper.assertTrue(level.getBlockState(origin.offset(CinderWolfService.EXIT_MARKER_LOCAL))
                .is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK), "authored exit marker");
        service.initialize(level, session, origin, bounds);
        int fireproof = 0;
        for (var pos : net.minecraft.core.BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            var state = level.getBlockState(pos);
            helper.assertTrue(!state.is(net.minecraft.world.level.block.Blocks.STRIPPED_MANGROVE_WOOD),
                    "authored mangrove wood must all be fireproofed");
            if (state.is(com.cosmicpve.registry.ModBlocks.CINDERPROOF_MANGROVE_WOOD.get())) {
                fireproof++;
                helper.assertTrue(((net.minecraft.world.level.block.FireBlock) net.minecraft.world.level.block.Blocks.FIRE)
                        .getBurnOdds(state) == 0, "room wood cannot be consumed by fire");
            }
        }
        helper.assertTrue(fireproof == 1695, "all 1695 authored mangrove blocks retain their visual material");
        helper.assertTrue(level.getBlockState(origin.offset(CinderWolfService.BOSS_MARKER_LOCAL))
                .is(net.minecraft.world.level.block.Blocks.NETHER_BRICKS), "boss marker replacement");
        var area = new net.minecraft.world.phys.AABB(origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + 34, origin.getY() + 21, origin.getZ() + 35);
        var boss = level.getEntitiesOfClass(com.cosmicpve.entity.cinderwolf.CinderWolfEntity.class, area,
                entity -> entity.getType() == com.cosmicpve.registry.ModEntities.CINDER_WOLF.get()).getFirst();
        helper.assertTrue(boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) == 200
                && boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == 2.5
                && boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS) == 1
                && boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) == .35
                && boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == 7.5,
                "solo boss attributes");
        helper.assertTrue(Math.abs(boss.getBbWidth() - .6F * 2.25F) < .001F
                && Math.abs(boss.getBbHeight() - .85F * 2.25F) < .001F,
                "boss collision box tracks its 2.25x visual scale");
        helper.assertTrue(boss.fireImmune(), "boss fire immunity");
        service.activate(level, session);
        for (int tick = 1; tick < 160; tick++) service.tick(level, session);
        helper.assertTrue(projectiles(level, area) == 0, "no early fireball volley");
        service.tick(level, session);
        helper.assertTrue(projectiles(level, area) == 4, "four cardinal fireballs at tick 160");
        for (int tick = 161; tick <= 240; tick++) service.tick(level, session);
        var pups = level.getEntitiesOfClass(com.cosmicpve.entity.cinderwolf.CinderWolfEntity.class, area,
                entity -> entity.getType() == com.cosmicpve.registry.ModEntities.CINDER_PUP.get());
        helper.assertTrue(pups.size() == 3,
                "first solo wave has three pups");
        helper.assertTrue(pups.stream().allMatch(pup -> pup.fireImmune()
                && pup.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) == 5
                && pup.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == 0
                && pup.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == 6
                && level.getBlockState(pup.blockPosition()).isAir()
                && level.getBlockState(pup.blockPosition().below()).isFaceSturdy(level,
                        pup.blockPosition().below(), net.minecraft.core.Direction.UP)),
                "pups have canonical stats, fire immunity, and safe floor spawns");
        for (int tick = 241; tick <= 480; tick++) service.tick(level, session);
        helper.assertTrue(level.getEntitiesOfClass(com.cosmicpve.entity.cinderwolf.CinderWolfEntity.class, area,
                entity -> entity.getType() == com.cosmicpve.registry.ModEntities.CINDER_PUP.get()).size() == 6,
                "surviving pups accumulate across waves");
        boss.setHealth(0);
        helper.assertTrue(service.bossDeath(level, session, boss), "boss death creates exit once");
        helper.assertTrue(!service.bossDeath(level, session, boss), "exit cannot be duplicated");
        helper.assertTrue(level.getBlockState(origin.offset(CinderWolfService.EXIT_MARKER_LOCAL).above())
                .is(com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get()), "exit portal at Diamond marker");
        service.cleanup(level, session.sessionId());
        helper.assertTrue(level.getEntitiesOfClass(com.cosmicpve.entity.cinderwolf.CinderWolfEntity.class, area,
                entity -> true).isEmpty() && projectiles(level, area) == 0, "cleanup owns boss, pups, and fireballs");
        helper.succeed();
    }

    private static int projectiles(net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.AABB area) {
        return level.getEntitiesOfClass(net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class,
                area, entity -> entity.getTags().contains(CinderWolfService.PROJECTILE_TAG)).size();
    }

    private static void pupAggro(GameTestHelper helper) {
        var player = mockPlayer(helper);
        var level = helper.getLevel();
        for (int x = 3; x <= 8; x++) for (int z = 3; z <= 5; z++)
            helper.setBlock(new net.minecraft.core.BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);
        var pup = com.cosmicpve.registry.ModEntities.CINDER_PUP.get().create(level,
                net.minecraft.world.entity.EntitySpawnReason.EVENT);
        helper.assertTrue(pup != null, "Pup can be created");
        var playerPos = helper.absolutePos(new net.minecraft.core.BlockPos(4, 2, 4));
        var pupPos = helper.absolutePos(new net.minecraft.core.BlockPos(7, 2, 4));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(playerPos.getX() + .5, playerPos.getY(), playerPos.getZ() + .5);
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        player.setHealth(100);
        pup.setPos(pupPos.getX() + .5, pupPos.getY(), pupPos.getZ() + .5);
        pup.setEncounterParticipants(java.util.Set.of(player.getUUID()));
        pup.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(5);
        pup.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(6);
        pup.setHealth(5);
        helper.assertTrue(level.addFreshEntity(pup), "Pup spawned");
        helper.runAfterDelay(60, () -> {
            try {
                helper.assertTrue(pup.isAlive(), "Pup remains alive without player damage");
                // Mock players never finish the client-loading handshake, so ServerPlayer rejects damage.
                helper.assertTrue(pup.getTarget() == player && pup.distanceTo(player) < 2.0
                        && pup.meleeAttempts() >= 2, "Pup naturally pursues and attempts repeated melee strikes");
            } finally {
                pup.discard();
                player.closeContainer();
                level.getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });
    }

    private static net.minecraft.server.level.ServerPlayer mockPlayer(GameTestHelper helper) {
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent> configure = event -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(
                        player.connection.getConnection());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.bus.api.EventPriority.HIGHEST, configure);
        try { return helper.makeMockServerPlayerInLevel(); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(configure); }
    }

    private static void deathCoffinChild(GameTestHelper helper) {
        var level = helper.getLevel();
        var attacker = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new net.minecraft.core.BlockPos(2, 2, 3));
        var original = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new net.minecraft.core.BlockPos(4, 2, 3));
        var nearby = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new net.minecraft.core.BlockPos(5, 2, 3));
        var distant = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new net.minecraft.core.BlockPos(8, 2, 3));
        attacker.setNoAi(true); original.setNoAi(true); nearby.setNoAi(true); distant.setNoAi(true);
        var source = level.damageSources().mobAttack(attacker);
        var context = new com.cosmicpve.combat.api.CombatContext(attacker, attacker, attacker, original,
                java.util.Optional.empty(), source, com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY, java.util.Set.of(),
                new com.cosmicpve.combat.api.WeaponSnapshot(new ItemStack(Items.DIAMOND_AXE)),
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY, 123L,
                java.util.OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
        var breakdown = new com.cosmicpve.combat.api.CombatBreakdown(4, 0, java.util.List.of(), 4,
                java.util.List.of(), 1, 4, 4, java.util.List.of(), java.util.List.of(), 1, 4, 4);
        var behavior = new com.cosmicpve.combat.enchantment.DeathCoffinBehavior(
                com.cosmicpve.combat.ownership.GeneralAllyResolver.production(),
                com.cosmicpve.combat.CosmicCombat.childActions());
        original.setHealth(10);
        var parent = new com.cosmicpve.combat.api.CombatResult(context, breakdown, java.util.List.of(), 1);
        helper.assertTrue(behavior.activate(parent, 1).isEmpty(), "above-threshold parent cannot chain");
        original.setHealth(5);
        float nearBefore = nearby.getHealth();
        float farBefore = distant.getHealth();
        helper.assertTrue(behavior.activate(parent, 1).contains(nearby), "low-health parent chains to nearby non-ally");
        helper.assertTrue(nearby.getHealth() < nearBefore && distant.getHealth() == farBefore,
                "radius I reaches only nearby entity");
        helper.assertTrue(original.getHealth() == 5, "original target is never double-hit");
        attacker.discard(); original.discard(); nearby.discard(); distant.discard();
        helper.succeed();
    }
}
