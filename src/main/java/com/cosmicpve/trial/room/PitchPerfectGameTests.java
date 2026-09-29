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
    static {
        FUNCTIONS.register("pitch_perfect_structure", ignored -> PitchPerfectGameTests::structure);
        FUNCTIONS.register("pitch_perfect_success_cue", ignored -> PitchPerfectGameTests::successCue);
    }
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
        event.registerTest(CosmicPVE.id("pitch_perfect_success_cue"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("pitch_perfect_success_cue")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
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
        helper.assertTrue(service.attempt(session.sessionId()).required() == 4, "one-player rounds fixed");
        service.cleanup(session.sessionId());
        helper.assertTrue(service.attempt(session.sessionId()) == null, "attempt cleaned");
        service.initialize(level, session, origin, bounds);
        helper.assertTrue(service.attempt(session.sessionId()) != null, "room reusable after cleanup");
        service.cleanup(session.sessionId());
        helper.succeed();
    }

    private static void successCue(GameTestHelper helper) {
        var member = mockPlayer(helper);
        var spectator = mockPlayer(helper);
        var service = new PitchPerfectService();
        var sessionId = UUID.randomUUID();
        packets(member); packets(spectator);
        service.scheduleSuccessCue(sessionId, List.of(member.getUUID()), 100);
        var pitches = new java.util.ArrayList<Float>();
        for (int tick = 100; tick <= 108; tick++) {
            service.tickSuccessCues(helper.getLevel().getServer(), tick);
            var sounds = packets(member).stream()
                    .filter(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance)
                    .map(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::cast).toList();
            if (tick % 2 == 0) helper.assertTrue(sounds.size() == 1, "one local note every two ticks");
            else helper.assertTrue(sounds.isEmpty(), "no interstitial note");
            for (var sound : sounds) {
                helper.assertTrue(sound.getSound().value() == net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(),
                        "success cue uses PLING");
                pitches.add(sound.getPitch());
            }
        }
        helper.assertTrue(pitches.equals(PitchPerfectService.SUCCESS_PITCHES), "exact five ascending cue pitches");
        helper.assertTrue(packets(spectator).stream().noneMatch(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance),
                "unrelated player receives no success cue");
        service.tickSuccessCues(helper.getLevel().getServer(), 110);
        helper.assertTrue(packets(member).stream().noneMatch(net.minecraft.network.protocol.game.ClientboundSoundPacket.class::isInstance),
                "finished cue does not repeat");
        helper.succeed();
    }

    private static net.minecraft.server.level.ServerPlayer mockPlayer(GameTestHelper helper) {
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent> configure = event -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(player.connection.getConnection());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, configure);
        try { return helper.makeMockServerPlayerInLevel(); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(configure); }
    }

    private static List<Object> packets(net.minecraft.server.level.ServerPlayer player) {
        var channel = (io.netty.channel.embedded.EmbeddedChannel) player.connection.getConnection().channel();
        channel.runPendingTasks(); var result = new java.util.ArrayList<Object>(); Object packet;
        while ((packet = channel.readOutbound()) != null) result.add(packet);
        return result;
    }
}
