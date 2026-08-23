package com.cosmicpve.trial;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.protection.InstanceProtectionService;
import com.cosmicpve.instance.structure.InstanceStructurePlacement;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import com.cosmicpve.trial.persistence.TrialSessionRepository;
import com.cosmicpve.trial.room.CircuitCircusService;
import com.cosmicpve.trial.room.CircuitPlacementPolicy;
import com.cosmicpve.trial.room.RaidingRainbowService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TrialSessionService {
    public static final BlockPos DECISION_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos ROOM_ORIGIN = new BlockPos(128, 64, 0);
    public static final Identifier DECISION_ROOM = CosmicPVE.id("trial/decision_box");
    public static final Identifier DEVELOPMENT_ROOM = CosmicPVE.id("trial/development_room");
    public static final Identifier RAIDING_RAINBOW = CosmicPVE.id("trial/raiding_rainbow");
    public static final Identifier CIRCUIT_CIRCUS = CosmicPVE.id("trial/circuit_circus");
    public static final Identifier APPRENTICE_REWARDS = CosmicPVE.id("trial/apprentice");
    private static final List<Identifier> APPRENTICE_ROOMS = List.of(RAIDING_RAINBOW, CIRCUIT_CIRCUS);
    static final int ROOM_REWARD_TIME = 600;
    static final int HARDCORE_PHASE_BONUS = 3_600;

    private final TrialSessionRepository repository;
    private final TrialInventoryTransactionService inventories;
    private final InstanceStructureService structures;
    private final InstanceProtectionService protection;
    private final TrialTitleService titles;
    private final TrialRoomLoadoutService loadouts;
    private final TrialRoomSelectionService selection = new TrialRoomSelectionService();
    private final RewardDeliveryService delivery = new RewardDeliveryService();
    private final RewardTableService rewards = new RewardTableService(CosmicContent.repository(), new RewardGeneratorService());
    private final RaidingRainbowService rainbow = new RaidingRainbowService();
    private final CircuitCircusService circuit = new CircuitCircusService();
    private final Map<UUID, BlockPos> decisionSpawns = new HashMap<>();
    private final Map<UUID, BlockPos> roomSpawns = new HashMap<>();

    public TrialSessionService(TrialSessionRepository repository, TrialInventoryTransactionService inventories,
            InstanceStructureService structures, InstanceProtectionService protection, TrialTitleService titles) {
        this.repository = repository; this.inventories = inventories; this.structures = structures;
        this.protection = protection; this.titles = titles; this.loadouts = new TrialRoomLoadoutService(inventories);
    }

    public Optional<TrialSession> active(MinecraftServer server) { return repository.active(server); }

    public TrialOperationResult createPortal(ServerPlayer owner, BlockPos bottom) {
        MinecraftServer server = owner.level().getServer();
        if (owner.level().dimension().equals(TrialRuntime.INSTANCE_DIMENSION))
            return TrialOperationResult.rejected("Trial Portals cannot be placed inside the instance dimension.");
        if (active(server).isPresent()) return TrialOperationResult.rejected("A Trial is already active.");
        if (!owner.level().getBlockState(bottom).canBeReplaced() || !owner.level().getBlockState(bottom.above()).canBeReplaced())
            return TrialOperationResult.rejected("The Trial Portal needs two clear vertical blocks.");
        ServerLevel instance = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (instance == null) return TrialOperationResult.rejected("The Cosmic instance dimension is unavailable.");
        InstanceStructurePlacement placedDecision = null;
        try {
            placedDecision = structures.place(instance, CosmicContent.repository().requireTrialRoom(DECISION_ROOM), DECISION_ORIGIN);
            List<BlockPos> portalBlocks = List.of(bottom.immutable(), bottom.above().immutable());
            TrialSession session = TrialSession.joining(UUID.randomUUID(), owner.level().dimension().identifier(), bottom,
                    portalBlocks, List.of(placedDecision.bounds()));
            owner.level().setBlock(bottom, com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
            owner.level().setBlock(bottom.above(), com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
            repository.publish(server, session); decisionSpawns.put(session.sessionId(), placedDecision.participantSpawn());
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
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null) return TrialOperationResult.rejected("This Trial Portal is no longer active.");
        if (!session.portalBlocks().contains(gatewayPos)) return TrialOperationResult.rejected("This gateway is not the active Trial Portal.");
        if (session.activeParticipant(player.getUUID())) return TrialOperationResult.ok("Already joined.");
        if (!session.acceptsJoins()) return TrialOperationResult.rejected("The Trial joining period has ended.");
        if (session.participants().size() >= TrialSession.MAX_PARTICIPANTS) return TrialOperationResult.rejected("This Trial party is full.");
        if (!inventories.enter(player, session.sessionId())) return TrialOperationResult.rejected("Could not durably snapshot your inventory.");
        try {
            TrialSession joined = session.addParticipant(player.getUUID()); repository.publish(player.level().getServer(), joined);
            teleport(player, decisionSpawn(joined));
            titles.decision(player, true, Math.max(1, (joined.stateTicksRemaining() + 19) / 20));
            return TrialOperationResult.ok("Joined Trial " + joined.sessionId());
        } catch (RuntimeException exception) {
            inventories.restore(player); return TrialOperationResult.rejected("Trial entry failed; your outside inventory was restored.");
        }
    }

    public void tick(MinecraftServer server) {
        TrialSession session = active(server).orElse(null); if (session == null) return;
        switch (session.state()) {
            case JOINING, DECISION -> tickDecision(server, session);
            case ROOM_INTRO -> tickRoomIntro(server, session);
            case ROOM_ACTIVE -> {
                int remaining = TrialStateMachine.tickGameplayTimer(session).timerTicks();
                if (remaining == 0) abort(server, "Trial timer expired."); else publishTick(server, session.withTimer(remaining));
            }
            default -> { }
        }
    }

    private void tickDecision(MinecraftServer server, TrialSession session) {
        int current = session.stateTicksRemaining();
        if (session.initialDecision()) {
            if (current >= 520 && current % 20 == 0)
                forOnline(server, session, player -> titles.decision(player, true, current / 20));
            int next = Math.max(0, current - 1);
            if (next == 0) { if (session.participants().isEmpty()) cleanupAndClose(server, session); else beginNextRoom(server, session); }
            else publishTick(server, session.withStateTicks(next));
            return;
        }
        if (current >= 520 && current % 20 == 0)
            forOnline(server, session, player -> titles.decision(player, false, current / 20));
        if (server.getTickCount() % 20 == 0) forOnline(server, session, player -> {
            if (session.progress().decision(player.getUUID()) == TrialDecision.UNDECIDED
                    && !(player.containerMenu instanceof TrialDecisionMenu)) openDecision(player);
        });
        if (session.progress().phase() == TrialPhase.HARDCORE && session.progress().completedRooms() >= 4) return;
        int next = Math.max(0, current - 1);
        if (next == 0) timeoutDecisions(server, session); else publishTick(server, session.withStateTicks(next));
    }

    private void tickRoomIntro(MinecraftServer server, TrialSession session) {
        int current = session.stateTicksRemaining();
        if (current % 20 == 0 && current > 0) {
            int seconds = current / 20; String name = CosmicContent.repository()
                    .requireTrialRoom(session.currentRoom().orElseThrow()).displayName();
            forOnline(server, session, player -> titles.roomCountdown(player, name, seconds));
        }
        int next = Math.max(0, current - 1);
        if (next == 0) {
            TrialSession active = session.withState(TrialLifecycleState.ROOM_ACTIVE, 0, session.currentRoom(), false,
                    session.protectedBounds()); repository.publish(server, active);
            forOnline(server, active, player -> titles.roomStarted(player));
            if (active.currentRoom().filter(RAIDING_RAINBOW::equals).isPresent())
                rainbow.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active, roomBounds(active));
        } else publishTick(server, session.withStateTicks(next));
    }

    public TrialOperationResult completeRoom(MinecraftServer server) { return completeProductionRoom(server); }

    public TrialOperationResult completeProductionRoom(MinecraftServer server) {
        TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE)
            return TrialOperationResult.rejected("No active Trial room can be completed.");
        TrialSession ending = session.withState(TrialLifecycleState.ENDING, 0, session.currentRoom(), false,
                session.protectedBounds()); repository.publish(server, ending);
        try {
            List<ItemStack> reward = rewards.roll(APPRENTICE_REWARDS, 1,
                    new RewardGenerationContext(server.registryAccess(), net.minecraft.util.RandomSource.create(), null));
            TrialProgress nextProgress = session.progress().completeRoom(reward).beginDecision(session.participants());
            int bonus = completionTimeBonus(session.progress(), nextProgress);
            cleanupCurrentRoom(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), session);
            var decision = CosmicContent.repository().requireTrialRoom(DECISION_ROOM);
            InstanceBounds decisionBounds = InstanceBounds.from(decision.bounds().at(DECISION_ORIGIN));
            TrialSession next = ending.withTimerAndProgress(session.timerTicks() + bonus, nextProgress)
                    .withState(TrialLifecycleState.DECISION, 600, Optional.empty(), false, List.of(decisionBounds));
            repository.publish(server, next);
            forOnline(server, next, player -> { loadouts.clear(player); teleport(player, decisionSpawn(next));
                titles.decision(player, false, 30); openDecision(player); });
            return TrialOperationResult.ok("Room completed: one reward added and " + bonus / 20 + " seconds awarded.");
        } catch (RuntimeException exception) {
            CosmicPVE.LOGGER.error("Trial completion transaction failed before reward publication", exception);
            repository.publish(server, session); return TrialOperationResult.rejected("Room completion failed safely and may be retried.");
        }
    }

    static int completionTimeBonus(TrialProgress before, TrialProgress after) {
        return ROOM_REWARD_TIME + (after.phase() == TrialPhase.HARDCORE
                && !before.hardcoreBonusApplied() ? HARDCORE_PHASE_BONUS : 0);
    }

    public TrialOperationResult continueRoom(MinecraftServer server) {
        TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION || session.initialDecision())
            return TrialOperationResult.rejected("The Trial is not waiting in a post-room Decision Box.");
        if (session.progress().phase() == TrialPhase.HARDCORE)
            return TrialOperationResult.rejected("Hardcore rooms are not enabled until Step 6O. The pot remains safe; choose DEAL to leave.");
        beginNextRoom(server, session); return TrialOperationResult.ok("Continuing to the next Apprentice room.");
    }

    public TrialOperationResult decide(ServerPlayer player, TrialDecision decision) {
        MinecraftServer server = player.level().getServer(); TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION || session.initialDecision()
                || !session.activeParticipant(player.getUUID())) return TrialOperationResult.rejected("No active Trial decision is available.");
        if (session.progress().decision(player.getUUID()) != TrialDecision.UNDECIDED)
            return TrialOperationResult.rejected("Your Trial decision is already committed.");
        if (decision == TrialDecision.NO_DEAL && session.progress().phase() == TrialPhase.HARDCORE) {
            player.sendSystemMessage(Component.literal("Hardcore rooms are not enabled in Step 6N. Your pot is safe; DEAL remains available."));
            return TrialOperationResult.rejected("Hardcore room pool is not enabled yet.");
        }
        if (decision == TrialDecision.DEAL) {
            List<ItemStack> payout = session.progress().pot().stream().flatMap(entry -> entry.items().stream()).map(ItemStack::copy).toList();
            if (!inventories.prepareCashout(player, payout)) return TrialOperationResult.rejected("Could not durably prepare your Trial payout.");
            TrialProgress decided = session.progress().decide(player.getUUID(), TrialDecision.DEAL);
            TrialSession next = session.withProgress(decided).removeParticipant(player.getUUID());
            repository.publish(server, next); player.closeContainer();
            if (!inventories.restoreCashout(player, delivery)) return TrialOperationResult.rejected("Payout is safely pending recovery.");
            if (next.participants().isEmpty()) cleanupAndClose(server, next); else resolveIfReady(server, next);
            return TrialOperationResult.ok("DEAL accepted. Outside state restored and the full pot delivered.");
        }
        TrialSession next = session.withProgress(session.progress().decide(player.getUUID(), TrialDecision.NO_DEAL));
        repository.publish(server, next); player.closeContainer(); resolveIfReady(server, next);
        return TrialOperationResult.ok("NO DEAL recorded. The shared pot remains intact.");
    }

    private void timeoutDecisions(MinecraftServer server, TrialSession session) {
        TrialProgress progress = session.progress();
        for (UUID participant : session.participants())
            if (progress.decision(participant) == TrialDecision.UNDECIDED) progress = progress.decide(participant, TrialDecision.NO_DEAL);
        TrialSession next = session.withProgress(progress); repository.publish(server, next); resolveIfReady(server, next);
    }

    private void resolveIfReady(MinecraftServer server, TrialSession session) {
        if (session.participants().isEmpty()) { cleanupAndClose(server, session); return; }
        boolean ready = session.participants().stream().allMatch(id -> session.progress().decision(id) == TrialDecision.NO_DEAL);
        if (ready) beginNextRoom(server, session);
    }

    private void beginNextRoom(MinecraftServer server, TrialSession session) {
        if (session.progress().phase() != TrialPhase.APPRENTICE) {
            TrialSession gated = session.withState(TrialLifecycleState.DECISION, 0, Optional.empty(), false,
                    session.protectedBounds()); repository.publish(server, gated);
            forOnline(server, gated, player -> player.sendSystemMessage(Component.literal(
                    "Hardcore rooms are not enabled until Step 6O. Your pot remains safe in the Decision Box.")));
            return;
        }
        Identifier room = selection.select(session, APPRENTICE_ROOMS, net.minecraft.util.RandomSource.create()).orElse(null);
        if (room == null) {
            CosmicPVE.LOGGER.error("No eligible Apprentice Trial room for session {}", session.sessionId());
            forOnline(server, session, player -> player.sendSystemMessage(Component.literal(
                    "No eligible Trial room is available. The session and pot remain safe in the Decision Box.")));
            return;
        }
        removePortal(server, session); ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        var placed = structures.place(level, CosmicContent.repository().requireTrialRoom(room), ROOM_ORIGIN);
        roomSpawns.put(session.sessionId(), placed.participantSpawn());
        TrialEncounterState encounter = room.equals(RAIDING_RAINBOW)
                ? rainbow.initialize(level, session, placed.bounds(), net.minecraft.util.RandomSource.create())
                : circuit.initialize(level, ROOM_ORIGIN, placed.bounds(), net.minecraft.util.RandomSource.create());
        TrialProgress progress = session.progress().beginRoom(room, encounter);
        var bounds = new ArrayList<>(session.protectedBounds()); bounds.add(placed.bounds());
        TrialSession next = session.withProgress(progress).withState(TrialLifecycleState.ROOM_INTRO,
                TrialSession.ROOM_INTRO_TICKS, Optional.of(room), false, bounds);
        repository.publish(server, next);
        forOnline(server, next, player -> {
            if (room.equals(RAIDING_RAINBOW)) loadouts.applyRaidingRainbow(player); else loadouts.applyCircuitCircus(player);
            teleport(player, placed.participantSpawn());
        });
    }

    public void onRainbowZombieDeath(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level) || !RaidingRainbowService.encounterZombie(zombie)) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(RAIDING_RAINBOW::equals).isEmpty()) return;
        var result = rainbow.killed(level, session, zombie, roomBounds(session));
        if (result.color() == null) return;
        if (!result.correct()) broadcast(level.getServer(), session, "You killed a zombie in the wrong order!");
        else broadcast(level.getServer(), session, result.color().display() + " was correct! "
                + result.nextState().sequenceProgress() + "/8!");
        TrialSession next = session.withProgress(session.progress().withEncounter(result.nextState()));
        repository.publish(level.getServer(), next);
        if (result.complete()) completeProductionRoom(level.getServer());
    }

    public boolean onCircuitTarget(ServerPlayer player, BlockPos target) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE || !session.activeParticipant(player.getUUID())
                || session.currentRoom().filter(CIRCUIT_CIRCUS::equals).isEmpty()) return false;
        var result = circuit.hitTarget((ServerLevel) player.level(), player, ROOM_ORIGIN, target,
                session.progress().encounter(), net.minecraft.util.RandomSource.create());
        if (!result.accepted()) return false;
        repository.publish(player.level().getServer(), session.withProgress(session.progress().withEncounter(result.state())));
        return true;
    }

    public boolean allowsCircuitPlacement(ServerPlayer player, BlockPos pos, BlockState state) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && CircuitPlacementPolicy.allows(session.activeParticipant(player.getUUID()),
                session.currentRoom().filter(CIRCUIT_CIRCUS::equals).isPresent(), session.state(),
                roomBounds(session), pos, state);
    }

    public boolean allowsCircuitPlacementUse(ServerPlayer player, BlockPos placementPos, ItemStack held) {
        if (!(held.getItem() instanceof BlockItem blockItem)) return false;
        return allowsCircuitPlacement(player, placementPos, blockItem.getBlock().defaultBlockState());
    }

    public void onCircuitPlaced(ServerPlayer player, BlockPos pos, BlockState state) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || !allowsCircuitPlacement(player, pos, state)) return;
        var result = circuit.validateAfterPlacement((ServerLevel) player.level(), roomBounds(session), session.progress().encounter(), state.getBlock());
        if (!result.newlyCompleted()) return;
        TrialSession next = session.withProgress(session.progress().withEncounter(result.state())); repository.publish(player.level().getServer(), next);
        forOnline(player.level().getServer(), next, member -> member.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.0F));
    }

    public boolean allowsCircuitUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(CIRCUIT_CIRCUS::equals).isPresent()
                && pos.equals(ROOM_ORIGIN.offset(CircuitCircusService.LEVER_LOCAL));
    }

    public void onCircuitLever(ServerPlayer player, BlockPos pos) {
        if (!allowsCircuitUse(player, pos)) return;
        TrialSession session = active(player.level().getServer()).orElseThrow();
        if (circuit.allComplete(session.progress().encounter())) completeProductionRoom(player.level().getServer());
        else player.sendSystemMessage(Component.literal("All four circuits must be complete before using the lever."));
    }

    public TrialOperationResult exit(ServerPlayer player) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || !session.activeParticipant(player.getUUID())) return TrialOperationResult.rejected("You are not an active Trial participant.");
        loadouts.clear(player); if (!inventories.restore(player)) return TrialOperationResult.rejected("Your outside snapshot could not be restored safely.");
        TrialSession next = session.removeParticipant(player.getUUID());
        if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        return TrialOperationResult.ok("Exited the Trial and restored your outside state.");
    }

    public TrialOperationResult abort(MinecraftServer server, String reason) {
        TrialSession session = active(server).orElse(null); if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        forOnline(server, session, player -> { loadouts.clear(player); inventories.restore(player); }); cleanupAndClose(server, session);
        return TrialOperationResult.ok(reason);
    }
    public TrialOperationResult adjustTimer(MinecraftServer server, int ticks, boolean absolute) {
        TrialSession session = active(server).orElse(null); if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        long candidate = absolute ? ticks : (long) session.timerTicks() + ticks;
        int bounded = (int) Math.max(0, Math.min(Integer.MAX_VALUE, candidate)); repository.publish(server, session.withTimer(bounded));
        return TrialOperationResult.ok("Trial timer is now " + bounded + " ticks.");
    }
    public void onDisconnect(ServerPlayer player) {
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        });
    }
    public void onLogin(ServerPlayer player) {
        boolean active = active(player.level().getServer()).map(session -> session.activeParticipant(player.getUUID())).orElse(false);
        if (!active && inventories.pending(player).isPresent()) inventories.recover(player, delivery);
    }
    public void onDeath(ServerPlayer player) {
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            inventories.clearTrialInventory(player); TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        });
    }
    public void onRespawn(ServerPlayer player) { if (inventories.pending(player).isPresent()) inventories.recover(player, delivery); }
    public boolean emergencyRestore(ServerPlayer player) { return inventories.recover(player, delivery); }
    public void debugSound(ServerPlayer player, boolean roomStart) {
        if (roomStart) titles.debugRoomStartSound(player); else titles.debugCountdownSound(player);
    }
    public void recoverInterrupted(MinecraftServer server) {
        active(server).ifPresent(session -> { CosmicPVE.LOGGER.warn("Aborting interrupted Trial {} to preserve player snapshots", session.sessionId());
            cleanupWorldState(server, session); repository.clear(server); });
    }

    private void openDecision(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new TrialDecisionMenu(id, inventory),
                Component.literal("Trial Pot — DEAL or NO DEAL")), buffer -> buffer.writeInt(TrialDecisionMenu.SLOT_COUNT));
    }
    private void cleanupAndClose(MinecraftServer server, TrialSession session) {
        cleanupWorldState(server, session); repository.clear(server); decisionSpawns.remove(session.sessionId());
        roomSpawns.remove(session.sessionId()); protection.clearExplicitAllows();
    }
    private void cleanupWorldState(MinecraftServer server, TrialSession session) {
        removePortal(server, session); ServerLevel instance = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (instance != null) session.protectedBounds().forEach(bounds -> structures.cleanup(instance, bounds));
    }
    private void cleanupCurrentRoom(ServerLevel level, TrialSession session) {
        if (level == null || session.currentRoom().isEmpty()) return;
        InstanceBounds decision = InstanceBounds.from(CosmicContent.repository().requireTrialRoom(DECISION_ROOM).bounds().at(DECISION_ORIGIN));
        session.protectedBounds().stream().filter(bounds -> !bounds.equals(decision)).forEach(bounds -> structures.cleanup(level, bounds));
    }
    private void removePortal(MinecraftServer server, TrialSession session) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, session.portalDimension()));
        if (level != null) session.portalBlocks().forEach(pos -> { if (level.getBlockState(pos).is(com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get()))
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3); });
    }
    private BlockPos decisionSpawn(TrialSession session) { return decisionSpawns.getOrDefault(session.sessionId(), DECISION_ORIGIN.offset(23, 14, 23)); }
    private static InstanceBounds roomBounds(TrialSession session) {
        return session.protectedBounds().stream().filter(bounds -> bounds.min().getX() >= ROOM_ORIGIN.getX()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Active Trial room has no protected bounds"));
    }
    private static void teleport(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level().getServer().getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (level == null || !player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.<Relative>of(), 0.0F, 0.0F, false)) throw new IllegalStateException("Could not teleport Trial participant");
    }
    private static void forOnline(MinecraftServer server, TrialSession session, java.util.function.Consumer<ServerPlayer> action) {
        session.participants().stream().map(server.getPlayerList()::getPlayer).filter(java.util.Objects::nonNull).forEach(action);
    }
    private static void broadcast(MinecraftServer server, TrialSession session, String message) {
        forOnline(server, session, player -> player.sendSystemMessage(Component.literal(message)));
    }
    private void publishTick(MinecraftServer server, TrialSession session) {
        repository.publishVolatile(server, session); if (server.getTickCount() % 20 == 0) repository.flush(server);
    }
}
