package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialSession;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime smoke coverage for the authored structure and reusable attempt lifecycle. */
public final class PitchPerfectGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("pitch_perfect_structure", ignored -> PitchPerfectGameTests::structure); }
    private PitchPerfectGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(PitchPerfectGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("pitch_perfect_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("pitch_perfect_structure"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("pitch_perfect_structure")),
                new TestData<>(environment, CosmicPVE.id("trial/pitch_perfect"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void structure(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        var bounds = new InstanceBounds(origin, origin.offset(16, 9, 20));
        BlockPos marker = origin.offset(PitchPerfectService.SPAWN_MARKER_LOCAL);
        helper.assertTrue(level.getBlockState(marker).is(Blocks.EMERALD_BLOCK), "authored spawn marker");
        for (BlockPos plate : PitchPerfectService.TARGET_PLATES)
            helper.assertTrue(level.getBlockState(origin.offset(plate)).is(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
                    "authored target plate");
        for (var pair : PitchPerfectService.PORTAL_BUTTONS)
            for (BlockPos button : pair)
                helper.assertTrue(level.getBlockState(origin.offset(button)).is(Blocks.BAMBOO_BUTTON),
                        "authored replay button");
        level.setBlockAndUpdate(marker, Blocks.BLACKSTONE.defaultBlockState());
        var session = TrialSession.joining(UUID.randomUUID(),
                net.minecraft.resources.Identifier.withDefaultNamespace("overworld"), origin,
                List.of(), List.of(bounds)).addParticipant(UUID.randomUUID());
        var service = new PitchPerfectService();
        service.initialize(level, session, origin, bounds);
        helper.assertTrue(service.attempt(session.sessionId()) != null, "attempt initialized");
        helper.assertTrue(service.attempt(session.sessionId()).required() == 3, "one-player rounds fixed");
        service.cleanup(session.sessionId());
        helper.assertTrue(service.attempt(session.sessionId()) == null, "attempt cleaned");
        service.initialize(level, session, origin, bounds);
        helper.assertTrue(service.attempt(session.sessionId()) != null, "room reusable after cleanup");
        service.cleanup(session.sessionId());
        helper.succeed();
    }
}
