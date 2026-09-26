package com.cosmicpve.trial;

import com.cosmicpve.CosmicPVE;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Decision Box timeout must dismiss only its own stale menu. */
public final class TrialDecisionTimeoutGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("trial_decision_timeout_menu", ignored -> TrialDecisionTimeoutGameTests::verify); }

    private TrialDecisionTimeoutGameTests() {}
    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(TrialDecisionTimeoutGameTests::registerTests);
    }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("trial_decision_timeout_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("trial_decision_timeout_menu"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("trial_decision_timeout_menu")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void verify(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            TrialSession session = TrialSession.joining(UUID.randomUUID(),
                    net.minecraft.resources.Identifier.withDefaultNamespace("overworld"),
                    BlockPos.ZERO, List.of(), List.of()).addParticipant(player.getUUID());
            player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                    new TrialDecisionMenu(id, inventory), Component.literal("Trial decision")));
            helper.assertTrue(player.containerMenu instanceof TrialDecisionMenu, "decision menu opened");
            TrialSessionService.closeDecisionMenus(helper.getLevel().getServer(), session);
            helper.assertTrue(!(player.containerMenu instanceof TrialDecisionMenu),
                    "timeout dismisses the stale decision menu");

            player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                    ChestMenu.threeRows(id, inventory, new SimpleContainer(27)), Component.literal("Unrelated")));
            helper.assertTrue(player.containerMenu instanceof ChestMenu, "unrelated menu opened");
            TrialSessionService.closeDecisionMenus(helper.getLevel().getServer(), session);
            helper.assertTrue(player.containerMenu instanceof ChestMenu, "unrelated menu stays open");
        } finally {
            player.closeContainer();
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }
    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        Consumer<net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent> configure = event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(
                        player.connection.getConnection());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.bus.api.EventPriority.HIGHEST, configure);
        try { return helper.makeMockServerPlayerInLevel(); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(configure); }
    }
}
