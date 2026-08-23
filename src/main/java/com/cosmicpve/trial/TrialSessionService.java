package com.cosmicpve.trial;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.protection.InstanceProtectionService;
import com.cosmicpve.instance.structure.InstanceStructurePlacement;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import com.cosmicpve.trial.persistence.TrialSessionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class TrialSessionService {
    public static final BlockPos DECISION_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos ROOM_ORIGIN = new BlockPos(128, 64, 0);
    public static final net.minecraft.resources.Identifier DECISION_ROOM = CosmicPVE.id("trial/decision_box");
    public static final net.minecraft.resources.Identifier DEVELOPMENT_ROOM = CosmicPVE.id("trial/development_room");

    private final TrialSessionRepository repository;
    private final TrialInventoryTransactionService inventories;
    private final InstanceStructureService structures;
    private final InstanceProtectionService protection;
    private final TrialTitleService titles;
    private final TrialRoomLoadoutService loadouts;
    private final Map<UUID, BlockPos> decisionSpawns = new HashMap<>();
    private final Map<UUID, BlockPos> roomSpawns = new HashMap<>();

    public TrialSessionService(TrialSessionRepository repository, TrialInventoryTransactionService inventories,
            InstanceStructureService structures, InstanceProtectionService protection, TrialTitleService titles) {
        this.repository = repository;
        this.inventories = inventories;
        this.structures = structures;
        this.protection = protection;
        this.titles = titles;
        this.loadouts = new TrialRoomLoadoutService(inventories);
    }

    public Optional<TrialSession> active(MinecraftServer server) { return repository.active(server); }

    public TrialOperationResult createPortal(ServerPlayer owner, BlockPos bottom) {
        MinecraftServer server = owner.level().getServer();
        if (owner.level().dimension().equals(TrialRuntime.INSTANCE_DIMENSION)) {
            return TrialOperationResult.rejected("Trial Portals cannot be placed inside the instance dimension.");
        }
        if (active(server).isPresent()) return TrialOperationResult.rejected("A Trial is already active.");
        if (!owner.level().getBlockState(bottom).canBeReplaced()
                || !owner.level().getBlockState(bottom.above()).canBeReplaced()) {
            return TrialOperationResult.rejected("The Trial Portal needs two clear vertical blocks.");
        }
        ServerLevel instance = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (instance == null) return TrialOperationResult.rejected("The Cosmic instance dimension is unavailable.");
        InstanceStructurePlacement placedDecision = null;
        try {
            var decision = CosmicContent.repository().requireTrialRoom(DECISION_ROOM);
            placedDecision = structures.place(instance, decision, DECISION_ORIGIN);
            List<BlockPos> portalBlocks = List.of(bottom.immutable(), bottom.above().immutable());
            var session = TrialSession.joining(UUID.randomUUID(), owner.level().dimension().identifier(), bottom,
                    portalBlocks, List.of(placedDecision.bounds()));
            owner.level().setBlock(bottom, com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
            owner.level().setBlock(bottom.above(), com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
            repository.publish(server, session);
            decisionSpawns.put(session.sessionId(), placedDecision.participantSpawn());
            return TrialOperationResult.ok("Trial Portal created. Up to four players have 30 seconds to join.");
        } catch (RuntimeException exception) {
            CosmicPVE.LOGGER.error("Could not create Trial Portal", exception);
            owner.level().setBlock(bottom, Blocks.AIR.defaultBlockState(), 3);
            owner.level().setBlock(bottom.above(), Blocks.AIR.defaultBlockState(), 3);
            if (placedDecision != null) structures.cleanup(instance, placedDecision.bounds());
            return TrialOperationResult.rejected("Trial creation failed safely; no portal was consumed.");
        }
    }

    public TrialOperationResult join(ServerPlayer player, BlockPos gatewayPos) {
        var optional = active(player.level().getServer());
        if (optional.isEmpty()) return TrialOperationResult.rejected("This Trial Portal is no longer active.");
        TrialSession session = optional.orElseThrow();
        if (!session.portalBlocks().contains(gatewayPos)) return TrialOperationResult.rejected("This gateway is not the active Trial Portal.");
        if (session.activeParticipant(player.getUUID())) return TrialOperationResult.ok("Already joined.");
        if (!session.acceptsJoins()) return TrialOperationResult.rejected("The Trial joining period has ended.");
        if (session.participants().size() >= TrialSession.MAX_PARTICIPANTS) return TrialOperationResult.rejected("This Trial party is full.");
        if (!inventories.enter(player, session.sessionId())) return TrialOperationResult.rejected("Could not durably snapshot your inventory.");
        try {
            TrialSession joined = session.addParticipant(player.getUUID());
            repository.publish(player.level().getServer(), joined);
            teleport(player, decisionSpawn(joined));
            titles.decision(player, true, Math.max(1, (joined.stateTicksRemaining() + 19) / 20));
            return TrialOperationResult.ok("Joined Trial " + joined.sessionId());
        } catch (RuntimeException exception) {
            inventories.restore(player);
            return TrialOperationResult.rejected("Trial entry failed; your outside inventory was restored.");
        }
    }

    public void tick(MinecraftServer server) {
        var optional = active(server);
        if (optional.isEmpty()) return;
        TrialSession session = optional.orElseThrow();
        switch (session.state()) {
            case JOINING, DECISION -> {
                if (!session.initialDecision()) {
                    int current = session.stateTicksRemaining();
                    if (current >= 520 && current % 20 == 0) {
                        forOnline(server, session, player -> titles.decision(player, false, current / 20));
                    }
                    if (current > 0) publishTick(server, session.withStateTicks(current - 1));
                    return;
                }
                int current = session.stateTicksRemaining();
                if (current >= 520 && current % 20 == 0) {
                    forOnline(server, session, player -> titles.decision(player, true, current / 20));
                }
                int next = Math.max(0, session.stateTicksRemaining() - 1);
                if (next == 0) {
                    if (session.participants().isEmpty()) closeEmpty(server, session);
                    else beginDevelopmentRoom(server, session);
                } else publishTick(server, session.withStateTicks(next));
            }
            case ROOM_INTRO -> {
                int current = session.stateTicksRemaining();
                if (current % 20 == 0 && current > 0) {
                    int seconds = current / 20;
                    forOnline(server, session, player -> titles.roomCountdown(player,
                            CosmicContent.repository().requireTrialRoom(DEVELOPMENT_ROOM).displayName(), seconds));
                }
                int next = Math.max(0, current - 1);
                TrialSession updated = next == 0
                        ? session.withState(TrialLifecycleState.ROOM_ACTIVE, 0, session.currentRoom(), false,
                                session.protectedBounds())
                        : session.withStateTicks(next);
                if (next == 0) repository.publish(server, updated); else publishTick(server, updated);
            }
            case ROOM_ACTIVE -> {
                int remaining = TrialStateMachine.tickGameplayTimer(session).timerTicks();
                if (remaining == 0) abort(server, "Trial timer expired.");
                else publishTick(server, session.withTimer(remaining));
            }
            default -> { }
        }
    }

    public TrialOperationResult completeRoom(MinecraftServer server) {
        var optional = active(server);
        if (optional.isEmpty() || optional.orElseThrow().state() != TrialLifecycleState.ROOM_ACTIVE) {
            return TrialOperationResult.rejected("No active Trial room can be completed.");
        }
        TrialSession session = optional.orElseThrow();
        TrialSession ending = session.withState(TrialLifecycleState.ENDING, 0, session.currentRoom(), false,
                session.protectedBounds());
        repository.publish(server, ending); // duplicate completion calls now reject
        ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        cleanupCurrentRoom(level, session);
        var decision = CosmicContent.repository().requireTrialRoom(DECISION_ROOM);
        InstanceBounds decisionBounds = InstanceBounds.from(decision.bounds().at(DECISION_ORIGIN));
        forOnline(server, session, player -> {
            loadouts.clear(player);
            teleport(player, decisionSpawn(session));
            titles.decision(player, false, 30);
        });
        TrialSession next = ending.withState(TrialLifecycleState.DECISION, 600, Optional.empty(), false,
                List.of(decisionBounds));
        repository.publish(server, next);
        return TrialOperationResult.ok("Development room completed for the whole active party.");
    }

    public TrialOperationResult continueRoom(MinecraftServer server) {
        var optional = active(server);
        if (optional.isEmpty() || optional.orElseThrow().state() != TrialLifecycleState.DECISION
                || optional.orElseThrow().initialDecision()) {
            return TrialOperationResult.rejected("The Trial is not waiting in a post-room Decision Box.");
        }
        beginDevelopmentRoom(server, optional.orElseThrow());
        return TrialOperationResult.ok("Continuing to the development room.");
    }

    public TrialOperationResult exit(ServerPlayer player) {
        var optional = active(player.level().getServer());
        if (optional.isEmpty() || !optional.orElseThrow().activeParticipant(player.getUUID())) {
            return TrialOperationResult.rejected("You are not an active Trial participant.");
        }
        TrialSession session = optional.orElseThrow();
        loadouts.clear(player);
        if (!inventories.restore(player)) return TrialOperationResult.rejected("Your outside snapshot could not be restored safely.");
        TrialSession next = session.removeParticipant(player.getUUID());
        if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next);
        else repository.publish(player.level().getServer(), next);
        return TrialOperationResult.ok("Exited the Trial and restored your outside state.");
    }

    public TrialOperationResult abort(MinecraftServer server, String reason) {
        var optional = active(server);
        if (optional.isEmpty()) return TrialOperationResult.rejected("No active Trial exists.");
        TrialSession session = optional.orElseThrow();
        forOnline(server, session, player -> { loadouts.clear(player); inventories.restore(player); });
        cleanupAndClose(server, session);
        return TrialOperationResult.ok(reason);
    }

    public TrialOperationResult adjustTimer(MinecraftServer server, int ticks, boolean absolute) {
        var optional = active(server);
        if (optional.isEmpty()) return TrialOperationResult.rejected("No active Trial exists.");
        TrialSession session = optional.orElseThrow();
        long candidate = absolute ? ticks : (long)session.timerTicks() + ticks;
        int bounded = (int)Math.max(0, Math.min(Integer.MAX_VALUE, candidate));
        repository.publish(server, session.withTimer(bounded));
        return TrialOperationResult.ok("Trial timer is now " + bounded + " ticks.");
    }

    public void onDisconnect(ServerPlayer player) {
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next);
            else repository.publish(player.level().getServer(), next);
        });
    }

    public void onLogin(ServerPlayer player) {
        boolean stillActive = active(player.level().getServer())
                .map(session -> session.activeParticipant(player.getUUID())).orElse(false);
        if (!stillActive && inventories.pending(player).isPresent()) inventories.restore(player);
    }

    public void onDeath(ServerPlayer player) {
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            inventories.clearTrialInventory(player); // prevents Trial-only drops without changing keepInventory
            TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next);
            else repository.publish(player.level().getServer(), next);
        });
    }

    public void onRespawn(ServerPlayer player) {
        if (inventories.pending(player).isPresent()) inventories.restore(player);
    }

    public void recoverInterrupted(MinecraftServer server) {
        active(server).ifPresent(session -> {
            CosmicPVE.LOGGER.warn("Aborting interrupted Trial {} to preserve player snapshots", session.sessionId());
            cleanupWorldState(server, session);
            repository.clear(server);
        });
    }

    private void beginDevelopmentRoom(MinecraftServer server, TrialSession session) {
        removePortal(server, session);
        ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        var definition = CosmicContent.repository().requireTrialRoom(DEVELOPMENT_ROOM);
        var placed = structures.place(level, definition, ROOM_ORIGIN);
        roomSpawns.put(session.sessionId(), placed.participantSpawn());
        var bounds = new java.util.ArrayList<>(session.protectedBounds());
        bounds.add(placed.bounds());
        forOnline(server, session, player -> {
            loadouts.applyDevelopment(player);
            teleport(player, placed.participantSpawn());
        });
        repository.publish(server, session.withState(TrialLifecycleState.ROOM_INTRO,
                TrialSession.ROOM_INTRO_TICKS, Optional.of(DEVELOPMENT_ROOM), false, bounds));
    }

    private void closeEmpty(MinecraftServer server, TrialSession session) { cleanupAndClose(server, session); }

    private void cleanupAndClose(MinecraftServer server, TrialSession session) {
        cleanupWorldState(server, session);
        repository.clear(server);
        decisionSpawns.remove(session.sessionId());
        roomSpawns.remove(session.sessionId());
        protection.clearExplicitAllows();
    }

    private void cleanupWorldState(MinecraftServer server, TrialSession session) {
        removePortal(server, session);
        ServerLevel instance = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (instance != null) session.protectedBounds().forEach(bounds -> structures.cleanup(instance, bounds));
    }

    private void cleanupCurrentRoom(ServerLevel level, TrialSession session) {
        if (level == null || session.currentRoom().isEmpty()) return;
        var decisionBounds = CosmicContent.repository().requireTrialRoom(DECISION_ROOM).bounds().at(DECISION_ORIGIN);
        session.protectedBounds().stream().filter(bounds -> !bounds.equals(InstanceBounds.from(decisionBounds)))
                .forEach(bounds -> structures.cleanup(level, bounds));
    }

    private void removePortal(MinecraftServer server, TrialSession session) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, session.portalDimension());
        ServerLevel level = server.getLevel(key);
        if (level != null) session.portalBlocks().forEach(pos -> {
            if (level.getBlockState(pos).is(com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        });
    }

    private BlockPos decisionSpawn(TrialSession session) {
        return decisionSpawns.getOrDefault(session.sessionId(), DECISION_ORIGIN.offset(23, 14, 23));
    }

    private static void teleport(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level().getServer().getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (level == null || !player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.<Relative>of(), 0.0F, 0.0F, false)) {
            throw new IllegalStateException("Could not teleport participant into Trial instance");
        }
    }

    private static void forOnline(MinecraftServer server, TrialSession session,
                                  java.util.function.Consumer<ServerPlayer> action) {
        session.participants().stream().map(server.getPlayerList()::getPlayer)
                .filter(java.util.Objects::nonNull).forEach(action);
    }

    private void publishTick(MinecraftServer server, TrialSession session) {
        repository.publishVolatile(server, session);
        if (server.getTickCount() % 20 == 0) repository.flush(server);
    }
}
