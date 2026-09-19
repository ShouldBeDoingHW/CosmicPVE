package com.cosmicpve.entity.woodlands;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.adventure.AdventureSurface;
import com.cosmicpve.adventure.DenseWoodlandsSessionService;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

/** Opt-in development probe; never runs during ordinary gameplay and cleans up only its own entities. */
public final class DenseWoodlandsSpawnProbe {
    private DenseWoodlandsSpawnProbe() {}
    public static void verify(MinecraftServer server) {
        var level = server.getLevel(DenseWoodlandsSessionService.DIMENSION);
        if (level == null) throw new IllegalStateException("Dense Woodlands missing");
        var rule = net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS;
        boolean originalRule = level.getGameRules().get(rule);
        try {
            server.getCommands().getDispatcher().execute("gamerule minecraft:spawn_mobs true", server.createCommandSourceStack());
            CosmicPVE.LOGGER.info("WOODLANDS_SPAWN_COMMAND_VERIFIED /gamerule minecraft:spawn_mobs true");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) {
            throw new IllegalStateException(failure);
        } finally {
            level.getGameRules().set(rule, originalRule, server);
        }
        var origin = AdventureSurface.nearby(level, 1920, 1920, 12).orElseThrow();
        var profile = new GameProfile(UUID.randomUUID(), "woodlands-probe");
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(server, level, profile, cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        new ServerGamePacketListenerImpl(server, connection, player, cookie);
        player.setPos(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
        player.setInvulnerable(true);
        var before = new java.util.HashSet<UUID>();
        for (var entity : level.getAllEntities()) before.add(entity.getUUID());
        level.addNewPlayer(player);
        int valid = 0, placement = 0, obstruction = 0;
        boolean deferred = false;
        try {
            verifyPots(player, origin);
            for (int i = 0; i < 256; i++) {
                double angle = i * Math.PI * 2 / 256;
                var pos = AdventureSurface.nearby(level, origin.getX() + (int)(48 * Math.cos(angle)),
                        origin.getZ() + (int)(48 * Math.sin(angle)), 2).orElse(null);
                if (pos == null) continue;
                valid++;
                var type = com.cosmicpve.registry.ModEntities.FOREST_FANATIC.get();
                if (net.minecraft.world.entity.SpawnPlacements.isSpawnPositionOk(type, level, pos)
                        && net.minecraft.world.entity.SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.random)) placement++;
                var mob = type.create(level, EntitySpawnReason.NATURAL);
                mob.setPos(pos.getX()+.5, pos.getY(), pos.getZ()+.5);
                if (mob.checkSpawnRules(level, EntitySpawnReason.NATURAL) && mob.checkSpawnObstruction(level)) obstruction++;
                NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER, level, pos);
            }
            int spawned = 0;
            for (var entity : level.getAllEntities()) if (!before.contains(entity.getUUID())
                    && (entity instanceof ForestFanaticEntity || entity instanceof DreadmaneEntity)) spawned++;
            CosmicPVE.LOGGER.info("WOODLANDS_NATIVE_SPAWN_PROBE valid={} placement={} obstruction={} spawned={} biomePool={} difficulty={} monsters={}",
                    valid, placement, obstruction, spawned, level.getBiome(origin).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap(),
                    level.getDifficulty(), level.isSpawningMonsters());
            if (spawned == 0) throw new IllegalStateException("Native Woodlands spawn probe produced zero mobs");
            var ownMobs = new java.util.ArrayList<Entity>();
            for (var entity : level.getAllEntities()) if (!before.contains(entity.getUUID())
                    && (entity instanceof ForestFanaticEntity || entity instanceof DreadmaneEntity)) ownMobs.add(entity);
            ownMobs.forEach(Entity::discard);
            player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
            level.getChunkSource().move(player);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    new java.util.function.Consumer<net.neoforged.neoforge.event.tick.ServerTickEvent.Post>() {
                private int ticks;
                @Override public void accept(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
                    if (++ticks != 200) return;
                    int automatic = 0;
                    var cleanup = new java.util.ArrayList<Entity>();
                    for (var entity : level.getAllEntities()) if (!before.contains(entity.getUUID())
                            && (entity instanceof ForestFanaticEntity || entity instanceof DreadmaneEntity)) { automatic++; cleanup.add(entity); }
                    var state = level.getChunkSource().getLastSpawnState();
                    CosmicPVE.LOGGER.info("WOODLANDS_AUTOMATIC_SPAWN_PROBE ticks={} spawned={} spawnableChunks={} counts={}",
                            ticks, automatic, state == null ? -1 : state.getSpawnableChunkCount(), state == null ? null : state.getMobCategoryCounts());
                    try {
                        if (automatic == 0) throw new IllegalStateException("Normal server ticks produced no Woodlands mobs");
                    } finally {
                        cleanup.forEach(Entity::discard);
                        level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                        server.halt(false);
                    }
                }
            });
            deferred = true;
        } finally {
            if (!deferred) {
                var cleanup = new java.util.ArrayList<Entity>();
                for (var entity : level.getAllEntities()) if (!before.contains(entity.getUUID())
                        && (entity instanceof ForestFanaticEntity || entity instanceof DreadmaneEntity)) cleanup.add(entity);
                cleanup.forEach(Entity::discard);
                level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                server.halt(false);
            }
        }
    }

    private static void verifyPots(ServerPlayer player, net.minecraft.core.BlockPos origin) {
        var level = player.level();
        var pos = origin.above();
        var oldState = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null) throw new IllegalStateException("Pot probe requires an empty fixture position");
        player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
                net.minecraft.core.Direction.UP, pos, false);
        var ownedDrops = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        try {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.DECORATED_POT.defaultBlockState());
            var pot = (net.minecraft.world.level.block.entity.DecoratedPotBlockEntity) level.getBlockEntity(pos);
            var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
            var held = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 3);
            player.setItemInHand(hand, held);
            player.gameMode.useItemOn(player, level, held, hand, hit);
            if (held.getCount() != 3 || !pot.getTheItem().isEmpty()) throw new IllegalStateException("Main-hand pot deposit was not blocked");
            var offhand = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 4);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, offhand);
            player.gameMode.useItemOn(player, level, offhand, net.minecraft.world.InteractionHand.OFF_HAND, hit);
            if (offhand.getCount() != 4 || !pot.getTheItem().isEmpty()) throw new IllegalStateException("Off-hand pot deposit was not blocked");
            var trapped = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
            trapped.setDamageValue(37);
            trapped.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Pot recovery fixture"));
            net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(trapped, enchants -> enchants.set(
                    level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING), 1));
            pot.setTheItem(trapped.copy());
            player.getInventory().clearContent();
            player.gameMode.useItemOn(player, level, player.getMainHandItem(), hand, hit);
            player.gameMode.useItemOn(player, level, player.getMainHandItem(), hand, hit);
            int recovered = player.getInventory().getNonEquipmentItems().stream()
                    .filter(stack -> net.minecraft.world.item.ItemStack.isSameItemSameComponents(stack, trapped))
                    .mapToInt(net.minecraft.world.item.ItemStack::getCount).sum();
            if (recovered != 1 || !pot.getTheItem().isEmpty() || !level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DECORATED_POT))
                throw new IllegalStateException("Pot recovery lost/duplicated metadata or destroyed the pot");
            player.getInventory().clearContent();
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
                player.getInventory().setItem(slot, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64));
            pot.setTheItem(trapped.copy());
            if (com.cosmicpve.adventure.WoodlandsPotInteractions.recover(player, pot) != 1
                    || com.cosmicpve.adventure.WoodlandsPotInteractions.recover(player, pot) != 0)
                throw new IllegalStateException("Full-inventory pot recovery was not exactly once");
            for (var entity : ((net.minecraft.server.level.ServerLevel)level).getAllEntities())
                if (entity instanceof net.minecraft.world.entity.item.ItemEntity item
                        && net.minecraft.world.item.ItemStack.isSameItemSameComponents(item.getItem(), trapped)) ownedDrops.add(item);
            if (ownedDrops.stream().mapToInt(item -> item.getItem().getCount()).sum() != 1)
                throw new IllegalStateException("Full-inventory pot recovery did not safely drop the item");
            CosmicPVE.LOGGER.info("WOODLANDS_POT_RUNTIME_PROOF mainDepositBlocked=true offhandDepositBlocked=true adventureRecovery=true metadataPreserved=true exactlyOnce=true potIntact=true overflowSafe=true");
        } finally {
            ownedDrops.forEach(Entity::discard);
            player.getInventory().clearContent();
            level.setBlockAndUpdate(pos, oldState);
        }
    }
}
