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
