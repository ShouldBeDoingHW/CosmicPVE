package com.cosmicpve.conquest;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModBlocks;
import com.cosmicpve.registry.ModItems;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Real-level regression coverage for the production Conquest placement transaction. */
public final class ConquestGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CREATION =
            FUNCTIONS.register("conquest_creation", ignored -> ConquestGameTests::creation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MINING =
            FUNCTIONS.register("conquest_mining", ignored -> ConquestGameTests::mining);

    private ConquestGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(ConquestGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("conquest_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("conquest_creation"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("conquest_creation")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("conquest_mining"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("conquest_mining")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    /** Exercises the real Survival destroy-progress path, including loaded tags and enchantment attributes. */
    private static void mining(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlock(target, ModBlocks.CONQUEST_CHEST.get().defaultBlockState(), 3);
        var state = level.getBlockState(target);
        helper.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE),
                "Conquest Chest must load into minecraft:mineable/pickaxe");
        helper.assertTrue(state.requiresCorrectToolForDrops(),
                "Conquest Chest must use the correct-tool mining path");

        Player ironMiner = survivalMiner(helper, target, new ItemStack(Items.IRON_PICKAXE));
        helper.assertTrue(ironMiner.getMainHandItem().isCorrectToolForDrops(state),
                "Iron Pickaxe must be recognized as a correct Conquest Chest tool");
        helper.assertValueEqual(ironMiner.getDestroySpeed(state, target), 6.0F,
                "Iron Pickaxe must receive its vanilla mining speed");
        int ironTicks = breakTicks(state, ironMiner, level, target);
        helper.assertTrue(ironTicks >= 400 && ironTicks <= 600,
                "Iron Pickaxe must break in 20-30 seconds; observed " + ironTicks + " ticks");

        ItemStack endgameTool = new ItemStack(Items.NETHERITE_PICKAXE);
        var efficiency = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.EFFICIENCY);
        EnchantmentHelper.updateEnchantments(endgameTool, mutable -> mutable.set(efficiency, 5));
        Player endgameMiner = survivalMiner(helper, target, endgameTool);
        EnchantmentHelper.forEachModifier(endgameTool, EquipmentSlot.MAINHAND, (attribute, modifier) ->
                Objects.requireNonNull(endgameMiner.getAttribute(attribute)).addTransientModifier(modifier));
        helper.assertValueEqual((float) endgameMiner.getAttributeValue(Attributes.MINING_EFFICIENCY), 26.0F,
                "Efficiency V must contribute its real +26 mining-efficiency attribute");
        helper.assertValueEqual(endgameMiner.getDestroySpeed(state, target), 35.0F,
                "Netherite Efficiency V must reach the real vanilla pre-calibration speed");
        int endgameTicks = breakTicks(state, endgameMiner, level, target);
        helper.assertTrue(endgameTicks >= 160 && endgameTicks <= 240,
                "Netherite Efficiency V must break in 8-12 seconds; observed " + endgameTicks + " ticks");
        CosmicPVE.LOGGER.info("Conquest Survival mining calibration: iron={} ticks ({}s), netherite Efficiency V={} ticks ({}s)",
                ironTicks, ironTicks / 20.0F, endgameTicks, endgameTicks / 20.0F);
        helper.succeed();
    }

    private static Player survivalMiner(GameTestHelper helper, BlockPos target, ItemStack tool) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(target.getX() + 0.5D, target.getY(), target.getZ() + 2.5D);
        player.setOnGround(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        return player;
    }

    private static int breakTicks(BlockState state, Player player, net.minecraft.world.level.BlockGetter level,
            BlockPos pos) {
        float accumulated = 0.0F;
        int ticks = 0;
        while (accumulated < 1.0F && ticks < 2_000) {
            accumulated += state.getDestroyProgress(player, level, pos);
            ticks++;
        }
        return ticks;
    }

    private static void creation(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(2, 3, 2));
        for (int deltaX = -4; deltaX <= 4; deltaX++) {
            for (int deltaZ = -4; deltaZ <= 4; deltaZ++) {
                BlockPos floor = target.offset(deltaX, -1, deltaZ);
                level.setBlock(floor, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                level.setBlock(floor.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(floor.above(2), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        level.setBlock(target, Blocks.SHORT_GRASS.defaultBlockState(), 3);

        var created = ConquestRuntime.events().createAt(level, ConquestOrigin.FLARE, target, false);
        helper.assertTrue(created.isPresent(), "valid flat surface must create a Conquest event");
        ConquestEvent event = created.orElseThrow();
        helper.assertTrue(level.getBlockState(target).is(ModBlocks.CONQUEST_CHEST.get()),
                "created event must physically contain the Conquest Chest block");
        helper.assertTrue(ConquestRuntime.events().find(level.getServer(), event.id()).isPresent(),
                "physically created chest must have persisted event state");
        helper.assertTrue(level.getBlockEntity(target) instanceof ConquestChestBlockEntity,
                "created chest must initialize its registered block entity");

        BlockPos blocked = target.offset(4, 0, 0);
        level.setBlock(blocked.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        level.setBlock(blocked, Blocks.STONE.defaultBlockState(), 3);
        int before = ConquestRuntime.events().active(level.getServer()).size();
        helper.assertTrue(ConquestRuntime.events().createAt(level, ConquestOrigin.FLARE, blocked, false).isEmpty(),
                "occupied nonreplaceable target must reject creation");
        helper.assertValueEqual(ConquestRuntime.events().active(level.getServer()).size(), before,
                "failed block placement must not register a ghost event");

        BlockPos buried = target.offset(-4, 0, 0);
        level.setBlock(buried, Blocks.AIR.defaultBlockState(), 3);
        for (var direction : net.minecraft.core.Direction.values())
            level.setBlock(buried.relative(direction), Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(ConquestRuntime.events().createAt(level, ConquestOrigin.FLARE, buried, false).isEmpty(),
                "a buried candidate without two exposed faces must reject creation");
        BlockPos lava = target.offset(0, 0, 4);
        level.setBlock(lava.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(lava, Blocks.LAVA.defaultBlockState(), 3);
        helper.assertTrue(ConquestRuntime.events().createAt(level, ConquestOrigin.FLARE, lava, false).isEmpty(),
                "a lava-filled candidate must reject creation");

        var player = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "conquest-test"), ClientInformation.createDefault());
        player.getAbilities().instabuild = false;
        player.setPos(target.getX() + 2.5D, target.getY(), target.getZ() + 2.5D);
        helper.assertTrue(ConquestRuntime.events().interact(player, target),
                "first real interaction must activate the event");
        int pirateCount = level.getEntitiesOfClass(com.cosmicpve.entity.spacepirate.SpacePirateEntity.class,
                new AABB(target).inflate(32.0D)).size();
        helper.assertTrue(pirateCount >= 3 && pirateCount <= 5,
                "first interaction must physically create three to five Space Pirates");
        List<net.minecraft.world.item.ItemStack> delivered = new ArrayList<>();
        helper.assertTrue(ConquestRuntime.events().complete(player, target,
                (ignored, rewards) -> rewards.forEach(stack -> delivered.add(stack.copy()))),
                "activated Conquest Chest must complete through the mining callback service");
        level.removeBlock(target, false);
        helper.assertTrue(ConquestRuntime.events().find(level.getServer(), event.id()).isEmpty(),
                "mining must complete and remove the active event");
        helper.assertTrue(level.getBlockState(target).isAir(),
                "mining must remove the physical Conquest Chest");
        helper.assertTrue(delivered.stream().anyMatch(stack -> stack.is(ModItems.BANKNOTE.get())),
                "completion must deliver the guaranteed Banknote");
        var upgradeCrystals = delivered.stream().filter(stack -> stack.is(ModItems.UPGRADE_CRYSTAL.get())).toList();
        helper.assertValueEqual(upgradeCrystals.size(), 1,
                "completion must deliver exactly one guaranteed Upgrade Crystal stack");
        helper.assertTrue(upgradeCrystals.getFirst().getCount() >= 2 && upgradeCrystals.getFirst().getCount() <= 4,
                "guaranteed Upgrade Crystal quantity must be uniformly sourced from 2-4");
        helper.assertValueEqual(delivered.size(), 5,
                "Conquest completion must deliver three table rolls plus two guaranteed groups");

        ItemStack rejectedFlare = new ItemStack(ModItems.CONQUEST_CHEST_FLARE.get(), 2);
        helper.assertValueEqual(ConquestFlareItem.finishUse(player, rejectedFlare, java.util.Optional.empty(),
                ignored -> {}), InteractionResult.FAIL, "failed Flare result must reject");
        helper.assertValueEqual(rejectedFlare.getCount(), 2, "failed Flare result must consume nothing");

        ItemStack flare = new ItemStack(ModItems.CONQUEST_CHEST_FLARE.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, flare);
        int eventsBeforeFlare = ConquestRuntime.events().active(level.getServer()).size();
        helper.assertValueEqual(((ConquestFlareItem) ModItems.CONQUEST_CHEST_FLARE.get())
                .useServer(player, InteractionHand.MAIN_HAND, ignored -> {}), InteractionResult.SUCCESS,
                "actual server Flare path must create an event on ordinary generated terrain");
        helper.assertValueEqual(flare.getCount(), 1, "successful Flare must consume exactly one item");
        var flareEvents = ConquestRuntime.events().active(level.getServer());
        helper.assertValueEqual(flareEvents.size(), eventsBeforeFlare + 1,
                "successful Flare must publish exactly one active event");
        ConquestEvent flareEvent = flareEvents.getLast();
        helper.assertTrue(level.getBlockState(flareEvent.chestPosition()).is(ModBlocks.CONQUEST_CHEST.get()),
                "actual Flare event must have a physical Conquest Chest");
        ConquestRuntime.events().expire(level, flareEvent.id());

        verifyCommandSpawn(helper, target, "cosmic conquest spawn-here", ConquestOrigin.FLARE);
        verifyCommandSpawn(helper, target, "cosmic conquest spawn natural", ConquestOrigin.NATURAL);
        verifyCommandSpawn(helper, target, "cosmic conquest spawn flare", ConquestOrigin.FLARE);
        helper.succeed();
    }

    private static void verifyCommandSpawn(GameTestHelper helper, BlockPos sourcePosition, String command,
            ConquestOrigin expectedOrigin) {
        var level = helper.getLevel();
        int before = ConquestRuntime.events().active(level.getServer()).size();
        var source = level.getServer().createCommandSourceStack().withLevel(level)
                .withPosition(Vec3.atCenterOf(sourcePosition));
        level.getServer().getCommands().performPrefixedCommand(source, command);
        var events = ConquestRuntime.events().active(level.getServer());
        helper.assertValueEqual(events.size(), before + 1, command + " must create exactly one event");
        ConquestEvent event = events.getLast();
        helper.assertValueEqual(event.origin(), expectedOrigin, command + " must preserve its requested origin");
        helper.assertTrue(level.getBlockState(event.chestPosition()).is(ModBlocks.CONQUEST_CHEST.get()),
                command + " must place a physical Conquest Chest");
        helper.assertTrue(level.getBlockEntity(event.chestPosition()) instanceof ConquestChestBlockEntity,
                command + " must initialize the custom block entity");
        level.getServer().getCommands().performPrefixedCommand(source, "cosmic conquest list");
        level.getServer().getCommands().performPrefixedCommand(source, "cosmic conquest inspect " + event.id());
        ConquestRuntime.events().expire(level, event.id());
    }
}
