package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModEnchantments;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry proof that Telekinesis routes only the accepted break's final drop payload. */
public final class TelekinesisGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FINAL_DROPS =
            FUNCTIONS.register("telekinesis_final_drops", ignored -> TelekinesisGameTests::finalDrops);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SUPERBREAKER =
            FUNCTIONS.register("superbreaker_activation", ignored -> TelekinesisGameTests::superbreakerActivation);

    private TelekinesisGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(TelekinesisGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("telekinesis_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("telekinesis_final_drops"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("telekinesis_final_drops")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("superbreaker_activation"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("superbreaker_activation")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void superbreakerActivation(GameTestHelper helper) {
        var player = connectedTestPlayer(helper);
        var service = new SuperbreakerService();
        var pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        helper.assertTrue(!service.activate(player, pickaxe), "Unenchanted pickaxe cannot activate");
        var holder = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(ModEnchantments.SUPERBREAKER);
        EnchantmentHelper.updateEnchantments(pickaxe, mutable -> mutable.set(holder, 1));
        helper.assertTrue(!service.activate(player, new ItemStack(Items.DIAMOND_SWORD)), "Wrong tool rejected");
        long tick = helper.getLevel().getServer().getTickCount();
        EnchantmentSuppressionService.GLOBAL.suppressEnchantment(player, CosmicPVE.id("test_suppression"),
                ModEnchantments.SUPERBREAKER.identifier(), 20, tick);
        helper.assertTrue(!service.activate(player, pickaxe), "Suppressed Superbreaker rejected");
        var activePlayer = connectedTestPlayer(helper);
        helper.assertTrue(service.activate(activePlayer, pickaxe), "Effective Superbreaker activates");
        var haste = activePlayer.getEffect(net.minecraft.world.effect.MobEffects.HASTE);
        helper.assertTrue(haste != null && haste.getAmplifier() == 4 && haste.getDuration() == 220,
                "Level I grants exactly 11 seconds of Haste V");
        long gameTime = helper.getLevel().getServer().overworld().getGameTime();
        helper.assertTrue(SuperbreakerService.remainingTicks(pickaxe, gameTime) == 2400,
                "First pickaxe owns its 120s cooldown");
        helper.assertTrue(!service.activate(activePlayer, pickaxe), "Same pickaxe cannot reactivate immediately");
        var replacement = new ItemStack(Items.IRON_PICKAXE);
        EnchantmentHelper.updateEnchantments(replacement, mutable -> mutable.set(holder, 10));
        helper.assertTrue(SuperbreakerService.remainingTicks(replacement, gameTime) == 0,
                "A separate enchanted pickaxe starts ready");
        helper.assertTrue(service.activate(activePlayer, replacement), "Second pickaxe activates independently");
        helper.assertTrue(activePlayer.getEffect(net.minecraft.world.effect.MobEffects.HASTE).getDuration() == 400,
                "Level X grants 20 seconds of Haste V");
        helper.assertTrue(SuperbreakerService.remainingTicks(replacement, gameTime) == 2400,
                "Second pickaxe receives its own cooldown");
        helper.assertTrue(SuperbreakerService.remainingTicks(pickaxe, gameTime) == 2400,
                "Second activation does not reset the first pickaxe");
        var third = new ItemStack(Items.STONE_PICKAXE);
        EnchantmentHelper.updateEnchantments(third, mutable -> mutable.set(holder, 1));
        helper.assertTrue(service.activate(activePlayer, third),
                "Third ready pickaxe can activate while longer Haste V is already active");
        helper.assertTrue(SuperbreakerService.remainingTicks(third, gameTime) == 2400
                && activePlayer.getEffect(net.minecraft.world.effect.MobEffects.HASTE).getDuration() == 400,
                "Third activation preserves longer Haste and starts only its own cooldown");
        helper.assertTrue(!service.activate(activePlayer, replacement), "Second pickaxe remains blocked");
        helper.assertTrue(SuperbreakerService.remainingTicks(pickaxe, gameTime) == 2400,
                "Rejected activation never alters another item's cooldown");
        helper.assertTrue(SuperbreakerService.remainingTicks(pickaxe.copy(), gameTime) == 2400,
                "Item cooldown survives a normal stack copy");
        helper.succeed();
    }

    private static void finalDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = connectedTestPlayer(helper);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.getInventory().clearContent();

        BlockPos target = helper.absolutePos(new BlockPos(2, 3, 2));
        level.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
        var unrelated = new ItemEntity(level, target.getX() + 0.5D, target.getY() + 0.5D,
                target.getZ() + 1.5D, new ItemStack(Items.DIRT));
        level.addFreshEntity(unrelated);

        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        var telekinesis = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(ModEnchantments.TELEKINESIS);
        EnchantmentHelper.updateEnchantments(pickaxe, mutable -> mutable.set(telekinesis, 1));
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

        helper.assertTrue(player.gameMode.destroyBlock(target), "survival block break must be accepted");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.COBBLESTONE)),
                "Telekinesis must insert the accepted break's final Cobblestone drop");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(2.0D)).stream()
                        .noneMatch(entity -> entity.getItem().is(Items.COBBLESTONE)),
                "a fully inserted final drop must not also remain in the world");
        helper.assertTrue(!unrelated.isRemoved() && unrelated.getItem().is(Items.DIRT),
                "Telekinesis must not vacuum an unrelated nearby ItemEntity");
        helper.succeed();
    }

    private static ServerPlayer connectedTestPlayer(GameTestHelper helper) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "telekinesis-test");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile,
                cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND) {
            { new io.netty.channel.embedded.EmbeddedChannel(this); }
            @Override public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {}
        };
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie);
        return player;
    }
}
