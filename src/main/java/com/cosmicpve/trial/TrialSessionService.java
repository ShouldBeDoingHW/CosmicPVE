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
import com.cosmicpve.trial.room.FireColonyService;
import com.cosmicpve.trial.room.ZeroGService;
import com.cosmicpve.trial.room.ColdSnapService;
import com.cosmicpve.trial.room.BombSquadService;
import com.cosmicpve.trial.room.HiddenGraveyardService;
import com.cosmicpve.trial.room.DeadeyeService;
import com.cosmicpve.trial.room.HazeAndSeekService;
import com.cosmicpve.trial.room.WarzoneGiantsService;
import com.cosmicpve.trial.room.CaveDivingService;
import com.cosmicpve.trial.room.InventorService;
import com.cosmicpve.trial.room.CinderWolfService;
import com.cosmicpve.entity.cinderwolf.CinderWolfEntity;
import com.cosmicpve.content.definition.reward.RewardEntry;
import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.entity.inventor.InventorEntity;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.execution.ExecutionCause;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ServerExplosion;
import com.cosmicpve.data.component.TrialPortalModifiers;

public final class TrialSessionService {
    public static final BlockPos DECISION_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos ROOM_ORIGIN = new BlockPos(128, 64, 0);
    public static final Identifier DECISION_ROOM = CosmicPVE.id("trial/decision_box");
    public static final Identifier DEVELOPMENT_ROOM = CosmicPVE.id("trial/development_room");
    public static final Identifier RAIDING_RAINBOW = CosmicPVE.id("trial/raiding_rainbow");
    public static final Identifier CIRCUIT_CIRCUS = CosmicPVE.id("trial/circuit_circus");
    public static final Identifier FIRE_COLONY = CosmicPVE.id("trial/fire_colony");
    public static final Identifier ZERO_G = CosmicPVE.id("trial/zero_g");
    public static final Identifier COLD_SNAP = CosmicPVE.id("trial/cold_snap");
    public static final Identifier BOMB_SQUAD = CosmicPVE.id("trial/bomb_squad");
    public static final Identifier HIDDEN_GRAVEYARD = CosmicPVE.id("trial/hidden_graveyard");
    public static final Identifier DEADEYE = CosmicPVE.id("trial/deadeye");
    public static final Identifier HAZE_AND_SEEK = CosmicPVE.id("trial/haze_seek");
    public static final Identifier WARZONE_GIANTS = CosmicPVE.id("trial/warzone_giants");
    public static final Identifier CAVE_DIVING = CosmicPVE.id("trial/cave_diving");
    public static final Identifier INVENTOR = CosmicPVE.id("trial/inventor");
    public static final Identifier CINDER_WOLF = CosmicPVE.id("trial/cinder_wolf");
    static final long HIDDEN_GRAVEYARD_WORLD_TIME = 18_000L;
    public static final Identifier APPRENTICE_REWARDS = CosmicPVE.id("trial/apprentice");
    public static final Identifier HARDCORE_REWARDS = CosmicPVE.id("trial/hardcore_development");
    public static final Identifier IMPOSSIBLE_REWARDS = CosmicPVE.id("trial/impossible");
    public static final Identifier DEMONIC_REWARDS = CosmicPVE.id("trial/demonic_development");
    static final List<Identifier> APPRENTICE_NATIVE_ROOMS = List.of(COLD_SNAP, CIRCUIT_CIRCUS, RAIDING_RAINBOW, ZERO_G);
    static final List<Identifier> HARDCORE_NATIVE_ROOMS = List.of(HAZE_AND_SEEK, BOMB_SQUAD, FIRE_COLONY, CINDER_WOLF);
    public static final RewardEntry CINDERWOLF_BELT_REWARD = new RewardEntry(5, 1, 1,
            new RewardDescriptor.StaticItem(CosmicPVE.id("cinderwolf_belt")));
    static final List<Identifier> IMPOSSIBLE_NATIVE_ROOMS = List.of(HIDDEN_GRAVEYARD, CAVE_DIVING, INVENTOR);
    static final List<Identifier> DEMONIC_NATIVE_ROOMS = List.of(WARZONE_GIANTS, DEADEYE);
    static final int APPRENTICE_REWARD_TIME = 600;
    static final int HARDCORE_REWARD_TIME = 300;
    static final int PHASE_ENTRY_BONUS = 2_400;
    private static final ExecutionCause DEADEYE_FALL = new ExecutionCause(CosmicPVE.id("trial_deadeye_fall"));
    private static final ExecutionCause WARZONE_FALL = new ExecutionCause(CosmicPVE.id("trial_warzone_fall"));

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
    private final FireColonyService fireColony = new FireColonyService();
    private final ZeroGService zeroG = new ZeroGService();
    private final ColdSnapService coldSnap = new ColdSnapService();
    private final BombSquadService bombSquad = new BombSquadService();
    private final HiddenGraveyardService hiddenGraveyard = new HiddenGraveyardService();
    private final DeadeyeService deadeye = new DeadeyeService();
    private final HazeAndSeekService haze = new HazeAndSeekService();
    private final WarzoneGiantsService warzone = new WarzoneGiantsService();
    private final CaveDivingService caveDiving = new CaveDivingService();
    private final InventorService inventor = new InventorService();
    private final CinderWolfService cinderWolf = new CinderWolfService();
    private final TrialTimerDisplayService timerDisplay = new TrialTimerDisplayService();
    private final TrialCelebrationService celebrations = new TrialCelebrationService();
    private final TrialDecisionEntryService decisionEntries = new TrialDecisionEntryService();
    private final TrialInsuranceService insurance = new TrialInsuranceService();
    private final TrialFameService fame = new TrialFameService();
    private final TrialPerformanceTracker performance = new TrialPerformanceTracker();
    private final Map<UUID, BlockPos> decisionSpawns = new HashMap<>();
    private final Map<UUID, BlockPos> roomSpawns = new HashMap<>();
    private final TrialRoomHandlerRegistry handlers = new TrialRoomHandlerRegistry();

    public TrialSessionService(TrialSessionRepository repository, TrialInventoryTransactionService inventories,
            InstanceStructureService structures, InstanceProtectionService protection, TrialTitleService titles) {
        this.repository = repository; this.inventories = inventories; this.structures = structures;
        this.protection = protection; this.titles = titles; this.loadouts = new TrialRoomLoadoutService(inventories);
        handlers.register(RAIDING_RAINBOW,loadouts::applyRaidingRainbow);
        handlers.register(CIRCUIT_CIRCUS,loadouts::applyCircuitCircus);
        handlers.register(FIRE_COLONY,loadouts::applyFireColony);
        handlers.register(ZERO_G,loadouts::applyZeroG);
        handlers.register(COLD_SNAP,loadouts::applyColdSnap);
        handlers.register(BOMB_SQUAD,loadouts::applyBombSquad);
        handlers.register(DEADEYE,loadouts::applyDeadeye);
        handlers.register(HAZE_AND_SEEK,loadouts::applyHazeAndSeek);
        handlers.register(WARZONE_GIANTS,loadouts::applyWarzoneGiants);
        handlers.register(CAVE_DIVING,loadouts::applyCaveDiving);
        handlers.register(INVENTOR,loadouts::applyInventor);
        handlers.register(CINDER_WOLF,loadouts::applyCinderWolf);
        handlers.register(HIDDEN_GRAVEYARD,loadouts::applyHiddenGraveyard);
        handlers.registerStarter(RAIDING_RAINBOW,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                rainbow.initialize(level,session,p.bounds(),net.minecraft.util.RandomSource.create()),p.participantSpawn()));
        handlers.registerStarter(CIRCUIT_CIRCUS,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                circuit.initialize(level,ROOM_ORIGIN,p.bounds(),net.minecraft.util.RandomSource.create()),p.participantSpawn()));
        handlers.registerStarter(ZERO_G,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                zeroG.initialize(level,session,p.bounds(),ROOM_ORIGIN),p.participantSpawn()));
        handlers.registerStarter(COLD_SNAP,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                coldSnap.initialize(level,session,ROOM_ORIGIN,p.bounds()),p.participantSpawn()));
        handlers.registerStarter(BOMB_SQUAD,(level,session,p) -> {
            var result = bombSquad.initialize(level,session,ROOM_ORIGIN,p.bounds(),net.minecraft.util.RandomSource.create());
            return new TrialRoomHandlerRegistry.Start(result.encounter(),result.participantSpawn());
        });
        handlers.registerStarter(HIDDEN_GRAVEYARD,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                hiddenGraveyard.initialize(level,session,ROOM_ORIGIN,p.bounds(),net.minecraft.util.RandomSource.create()).encounter(),p.participantSpawn()));
        handlers.registerStarter(DEADEYE,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                deadeye.initialize(level,session,ROOM_ORIGIN,p.participantSpawn().below(),p.bounds()),p.participantSpawn()));
        handlers.registerStarter(HAZE_AND_SEEK,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                haze.initialize(level,session,p.bounds(),net.minecraft.util.RandomSource.create()),p.participantSpawn()));
        handlers.registerStarter(WARZONE_GIANTS,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                warzone.initialize(level,session,p.bounds(),net.minecraft.util.RandomSource.create()),p.participantSpawn()));
        handlers.registerStarter(CAVE_DIVING,(level,session,p) -> new TrialRoomHandlerRegistry.Start(
                caveDiving.initialize(level,session,ROOM_ORIGIN,p.bounds(),net.minecraft.util.RandomSource.create()).encounter(),p.participantSpawn()));
        handlers.registerStarter(INVENTOR,(level,session,p) -> {
            inventor.initialize(level,session,ROOM_ORIGIN,p.bounds(),net.minecraft.util.RandomSource.create());
            return new TrialRoomHandlerRegistry.Start(TrialEncounterState.EMPTY,p.participantSpawn());
        });
        handlers.registerStarter(FIRE_COLONY,(level,session,p) -> {
            fireColony.initialize(level,session,p.bounds());
            return new TrialRoomHandlerRegistry.Start(TrialEncounterState.EMPTY,p.participantSpawn());
        });
        handlers.registerStarter(CINDER_WOLF,(level,session,p) -> {
            cinderWolf.initialize(level, session, ROOM_ORIGIN, p.bounds());
            return new TrialRoomHandlerRegistry.Start(TrialEncounterState.EMPTY,p.participantSpawn());
        });
    }

    public Optional<TrialSession> active(MinecraftServer server) { return repository.active(server); }

    public TrialOperationResult createPortal(ServerPlayer owner, BlockPos bottom) {
        return createPortal(owner, bottom, TrialPortalModifiers.EMPTY);
    }

    public TrialOperationResult createPortal(ServerPlayer owner, BlockPos bottom, TrialPortalModifiers modifiers) {
        MinecraftServer server = owner.level().getServer();
        if (modifiers == null || !modifiers.valid())
            return TrialOperationResult.rejected("The Trial Portal contains invalid modifier data.");
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
                    portalBlocks, List.of(placedDecision.bounds()),
                    new TrialOwner(owner.getUUID(), owner.getName().getString()), modifiers);
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
        com.cosmicpve.personalvault.PersonalVaultRuntime.access().onActivityEntered(
                player, com.cosmicpve.activity.ActivityType.TRIAL);
        if (!inventories.enter(player, session.sessionId())) return TrialOperationResult.rejected("Could not durably snapshot your inventory.");
        try {
            TrialSession joined = session.addParticipant(player.getUUID()); repository.publish(player.level().getServer(), joined);
            decisionEntries.enter(player, () -> teleport(player, decisionSpawn(joined)), () ->
                    titles.decision(player, true, Math.max(1, (joined.stateTicksRemaining() + 19) / 20)));
            timerDisplay.show(player, joined);
            return TrialOperationResult.ok("Joined Trial " + joined.sessionId());
        } catch (RuntimeException exception) {
            inventories.restore(player); return TrialOperationResult.rejected("Trial entry failed; your outside inventory was restored.");
        }
    }

    public void tick(MinecraftServer server) {
        celebrations.tick(server);
        TrialSession session = active(server).orElse(null);
        com.cosmicpve.trial.madness.MadnessRuntime.INSTANCE.tick(server,session);
        session = active(server).orElse(null); if (session == null) return;
        long started = System.nanoTime();
        try { tickActive(server, session); }
        finally { performance.record(System.nanoTime() - started, session); }
    }

    private void tickActive(MinecraftServer server, TrialSession session) {
        timerDisplay.update(server, session);
        switch (session.state()) {
            case JOINING, DECISION -> tickDecision(server, session);
            case ROOM_INTRO -> tickRoomIntro(server, session);
            case ROOM_ACTIVE -> {
                ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
                Identifier room = session.currentRoom().orElse(null);
                boolean maintenanceTick = server.getTickCount() % 10 == 0;
                if (maintenanceTick && HIDDEN_GRAVEYARD.equals(room)) hiddenGraveyard.tick(level, session);
                if (server.getTickCount() % CaveDivingService.VALIDATION_INTERVAL_TICKS == 0
                        && CAVE_DIVING.equals(room)) {
                    CaveDivingService.ValidationResult caveResult = caveDiving.tick(level, session);
                    if (caveResult == CaveDivingService.ValidationResult.REJECTED)
                        forOnline(server, session, player -> player.playSound(
                                CaveDivingService.REJECTION_SOUND, 1.0F, CaveDivingService.REJECTION_PITCH));
                    else if (caveResult == CaveDivingService.ValidationResult.SOLVED) {
                        completeProductionRoom(server); return;
                    }
                }
                if (maintenanceTick && FIRE_COLONY.equals(room)) fireColony.tick(level, session);
                if (INVENTOR.equals(room)) inventor.tick(level, session);
                if (CINDER_WOLF.equals(room) && cinderWolf.tick(level, session)) {
                    completeProductionRoom(server); return;
                }
                TrialSession current = session;
                if (HAZE_AND_SEEK.equals(room)) {
                    var result = haze.tick(level, session);
                    current = session.withProgress(session.progress().withEncounter(result.encounter()));
                    if (result.complete()) { repository.publish(server, current); completeProductionRoom(server); return; }
                }
                if (WARZONE_GIANTS.equals(room)) {
                    var result = warzone.tick(level, session);
                    if (result.warningCountdown() > 0 && result.warningColors().size() == 2)
                        forOnline(server, session, player -> titles.warzoneWarning(player,
                                result.warningColors().get(0), result.warningColors().get(1), result.warningCountdown()));
                    if (result.hazardSound()) forOnline(server, session, player -> TrialTitleService.playForPlayer(
                            player, net.minecraft.sounds.SoundEvents.AMBIENT_WARPED_FOREST_MOOD.value(), 2.0F, 1.0F));
                    current = session.withProgress(session.progress().withEncounter(result.encounter()));
                    repository.publishVolatile(server, current);
                    for (UUID fallen : result.fallenPlayers()) {
                        ServerPlayer player = server.getPlayerList().getPlayer(fallen);
                        if (player != null) CosmicCombat.executions().execute(player, WARZONE_FALL, null, null);
                    }
                    current = active(server).orElse(null);
                    if (current == null || current.currentRoom().filter(WARZONE_GIANTS::equals).isEmpty()) return;
                }
                if (DEADEYE.equals(room)) {
                    current = tickDeadeyeFalls(server, session);
                    if (current == null) return;
                }
                if (maintenanceTick && ZERO_G.equals(room)) {
                    zeroG.keepFixturesPinned(level, session);
                    ZeroGTickResult objectives = tickZeroGObjectives(server, level, session);
                    if (objectives.completed()) return;
                    current = objectives.session();
                }
                int remaining = TrialStateMachine.tickGameplayTimer(current).timerTicks();
                if (remaining == 0) failForTimeout(server, current); else publishTick(server, current.withTimer(remaining));
            }
            default -> { }
        }
    }

    private void tickDecision(MinecraftServer server, TrialSession session) {
        int countdown = session.stateTicksRemaining();
        if (countdown > 0 && countdown % 20 == 0 && TrialTitleService.shouldAnnounceDecision(countdown / 20))
            forOnline(server,session,player -> titles.decision(player,session.initialDecision(),countdown / 20));
        if (!session.progress().madness().options().isEmpty()) {
            tickMadness(server, session); return;
        }
        int current = session.stateTicksRemaining();
        if (session.initialDecision()) {
            int next = Math.max(0, current - 1);
            if (next == 0) {
                if (session.participants().isEmpty()) cleanupAndClose(server, session);
                else {
                    TrialSession processed = processInitialSkip(server, session);
                    if (processed.progress().initialSkipProcessed()) beginNextRoom(server, processed);
                    else publishTick(server, session.withStateTicks(0));
                }
            }
            else publishTick(server, session.withStateTicks(next));
            return;
        }
        if (server.getTickCount() % 20 == 0) forOnline(server, session, player -> {
            if (session.progress().decision(player.getUUID()) == TrialDecision.UNDECIDED
                    && !(player.containerMenu instanceof TrialDecisionMenu)) openDecision(player);
        });
        int next = Math.max(0, current - 1);
        if (next == 0) timeoutDecisions(server, session); else publishTick(server, session.withStateTicks(next));
    }

    private void tickRoomIntro(MinecraftServer server, TrialSession session) {
        lockIntroParticipants(server, session);
        int current = session.stateTicksRemaining();
        if (current % 20 == 0 && current > 0) {
            int seconds = current / 20; String name = CosmicContent.repository()
                    .requireTrialRoom(session.currentRoom().orElseThrow()).displayName();
            forOnline(server, session, player -> titles.roomCountdown(player, name, seconds));
        }
        int next = Math.max(0, current - 1);
        if (next == 0) {
            TrialSession active = session.withState(TrialLifecycleState.ROOM_ACTIVE, 0, session.currentRoom(), false,
                    session.protectedBounds());
            if (active.currentRoom().filter(CAVE_DIVING::equals).isPresent()) {
                ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
                if (level != null) caveDiving.cleanupStartupItems(level, active);
            }
            repository.publish(server, active);
            forOnline(server, active, player -> titles.roomStarted(player));
            if (active.currentRoom().filter(RAIDING_RAINBOW::equals).isPresent())
                rainbow.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active, roomBounds(active));
            if (active.currentRoom().filter(ZERO_G::equals).isPresent())
                zeroG.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active);
            if (active.currentRoom().filter(HAZE_AND_SEEK::equals).isPresent())
                haze.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active);
            if (active.currentRoom().filter(INVENTOR::equals).isPresent())
                inventor.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active);
            if (active.currentRoom().filter(CINDER_WOLF::equals).isPresent())
                cinderWolf.activate(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), active);
        } else publishTick(server, session.withStateTicks(next));
    }

    private void lockIntroParticipants(MinecraftServer server, TrialSession session) {
        BlockPos anchor = roomSpawns.get(session.sessionId());
        if (anchor == null) return;
        double x = anchor.getX() + 0.5D, y = anchor.getY(), z = anchor.getZ() + 0.5D;
        forOnline(server, session, player -> {
            player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            player.fallDistance = 0.0F;
            if (needsIntroCorrection(player.getX(), player.getY(), player.getZ(), x, y, z)) {
                player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
            }
        });
    }

    static boolean needsIntroCorrection(double x, double y, double z, double anchorX, double anchorY, double anchorZ) {
        double dx = x - anchorX, dy = y - anchorY, dz = z - anchorZ;
        return dx * dx + dy * dy + dz * dz > 1.0E-6D;
    }

    private TrialSession tickDeadeyeFalls(MinecraftServer server, TrialSession session) {
        var threshold = deadeye.fallThreshold(session.sessionId());
        if (threshold.isEmpty()) return session;
        for (UUID participant : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(participant);
            if (player != null && shouldExecuteDeadeyeFall(player.getY(), threshold.getAsInt())) {
                CosmicCombat.executions().execute(player, DEADEYE_FALL, null, null);
            }
        }
        return active(server).orElse(null);
    }

    static boolean shouldExecuteDeadeyeFall(double playerY, int thresholdY) { return playerY <= thresholdY; }

    public TrialOperationResult completeRoom(MinecraftServer server) { return completeProductionRoom(server); }

    public TrialOperationResult debugSetBaseFame(MinecraftServer server, long value) {
        TrialSession session = active(server).orElse(null);
        if (session == null || value < 0) return TrialOperationResult.rejected("No active Trial or invalid Fame value.");
        repository.publish(server, session.withProgress(session.progress().debugSetBaseFame(value)));
        return TrialOperationResult.ok("Trial base Fame set to " + value + ".");
    }

    public TrialOperationResult completeProductionRoom(MinecraftServer server) {
        TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE)
            return TrialOperationResult.rejected("No active Trial room can be completed.");
        TrialSession ending = session.withState(TrialLifecycleState.ENDING, 0, session.currentRoom(), false,
                session.protectedBounds()); repository.publish(server, ending);
        try {
            Identifier rewardTable = rewardTableFor(session.progress().phase());
            /* The active run phase is authoritative even when a lower-tier native room was selected. */
            var rewardContext = new RewardGenerationContext(server.registryAccess(), net.minecraft.util.RandomSource.create(), null);
            List<ItemStack> reward = session.currentRoom().filter(CINDER_WOLF::equals).isPresent()
                    && cinderWolf.physicallyCompleted(session.sessionId())
                    ? rewards.rollWithExtra(rewardTable, CINDERWOLF_BELT_REWARD, 1, rewardContext)
                    : rewards.roll(rewardTable, 1, rewardContext);
            long fameAward = fame.roll(session.progress().phase(), net.minecraft.util.RandomSource.create());
            TrialProgress nextProgress = session.progress().completeRoom(reward, fameAward).beginDecision(session.participants());
            int bonus = completionTimeBonus(session.progress(), nextProgress);
            cleanupCurrentRoom(server.getLevel(TrialRuntime.INSTANCE_DIMENSION), session);
            var decision = CosmicContent.repository().requireTrialRoom(DECISION_ROOM);
            InstanceBounds decisionBounds = InstanceBounds.from(decision.bounds().at(DECISION_ORIGIN));
            TrialSession next = ending.withTimerAndProgress(session.timerTicks() + bonus, nextProgress)
                    .withState(TrialLifecycleState.DECISION, 600, Optional.empty(), false, List.of(decisionBounds));
            repository.publish(server, next);
            forOnline(server, next, player -> {
                loadouts.clear(player); player.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
                decisionEntries.enter(player, () -> teleport(player, decisionSpawn(next)), () -> {
                    titles.decision(player, false, 30); openDecision(player);
                });
            });
            return TrialOperationResult.ok("Room completed: one reward added and " + bonus / 20 + " seconds awarded.");
        } catch (RuntimeException exception) {
            CosmicPVE.LOGGER.error("Trial completion transaction failed before reward publication", exception);
            repository.publish(server, session); return TrialOperationResult.rejected("Room completion failed safely and may be retried.");
        }
    }

    static int completionTimeBonus(TrialProgress before, TrialProgress after) {
        return TrialProgression.playedRoomBonusTicks(before.phase())
                + TrialProgression.phaseEntryTicks(before.completedRooms(), after.completedRooms());
    }

    static Identifier rewardTableFor(TrialPhase phase) {
        return switch (phase) {
            case APPRENTICE -> APPRENTICE_REWARDS;
            case HARDCORE -> HARDCORE_REWARDS;
            case IMPOSSIBLE -> IMPOSSIBLE_REWARDS;
            case DEMONIC -> DEMONIC_REWARDS;
        };
    }

    private TrialSession processInitialSkip(MinecraftServer server, TrialSession session) {
        TrialProgress progress = session.progress();
        if (progress.initialSkipProcessed()) return session;
        try {
            for (int i = 0; i < progress.portalModifiers().skipRooms(); i++) {
                List<ItemStack> reward = rewards.roll(rewardTableFor(TrialProgression.phaseForRoomOrdinal(progress.nextRoomOrdinal())), 1,
                        new RewardGenerationContext(server.registryAccess(), net.minecraft.util.RandomSource.create(), null));
                progress = progress.appendSkippedReward(reward);
            }
            int slowMoSeconds = session.participants().stream().map(server.getPlayerList()::getPlayer)
                    .filter(java.util.Objects::nonNull).mapToInt(player ->
                            new com.cosmicpve.upgrade.PlayerUpgradeService().tier(
                                    player, com.cosmicpve.upgrade.PlayerUpgrade.SLOW_MO) * 20).max().orElse(0);
            int entryBonus = TrialProgression.phaseEntryTicks(session.progress().completedRooms(), progress.completedRooms());
            TrialSession processed = session.withTimerAndProgress(session.timerTicks() + slowMoSeconds * 20 + entryBonus,
                    progress.markInitialSkipProcessed());
            repository.publish(server, processed);
            return processed;
        } catch (RuntimeException exception) {
            CosmicPVE.LOGGER.error("Could not apply initial Skip rewards for Trial {}", session.sessionId(), exception);
            forOnline(server, session, player -> player.sendSystemMessage(Component.literal(
                    "Initial Trial rewards could not be prepared. The session remains safe in the Decision Box.")));
            return session;
        }
    }

    public TrialOperationResult continueRoom(MinecraftServer server) {
        TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION || session.initialDecision()
                || !session.progress().madness().options().isEmpty())
            return TrialOperationResult.rejected("The Trial is not waiting in a post-room Decision Box.");
        beginNextRoom(server, session); return TrialOperationResult.ok("Continuing to the next "
                + session.progress().phase().getSerializedName() + " room.");
    }

    public TrialOperationResult decide(ServerPlayer player, TrialDecision decision) {
        MinecraftServer server = player.level().getServer(); TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION || session.initialDecision()
                || !session.progress().madness().options().isEmpty()
                || !session.activeParticipant(player.getUUID())) return TrialOperationResult.rejected("No active Trial decision is available.");
        if (session.progress().decision(player.getUUID()) != TrialDecision.UNDECIDED)
            return TrialOperationResult.rejected("Your Trial decision is already committed.");
        if (decision == TrialDecision.DEAL) {
            List<ItemStack> payout = session.progress().pot().stream().flatMap(entry -> entry.items().stream()).map(ItemStack::copy).toList();
            long fameAward;
            try { fameAward = fame.cashout(session.progress().baseFame(), session.progress().portalModifiers()); }
            catch (ArithmeticException overflow) { return TrialOperationResult.rejected("Your Fame payout could not be represented safely."); }
            long currentFame = new com.cosmicpve.economy.FameService().balance(player);
            if (fameAward > 0 && !com.cosmicpve.economy.FameService.canAdd(currentFame, fameAward))
                return TrialOperationResult.rejected("Your Fame payout would exceed the supported balance.");
            if (!inventories.prepareCashout(player, payout, fameAward)) return TrialOperationResult.rejected("Could not durably prepare your Trial payout.");
            TrialProgress decided = session.progress().decide(player.getUUID(), TrialDecision.DEAL);
            TrialSession next = session.withProgress(decided).removeParticipant(player.getUUID());
            repository.publish(server, next); player.closeContainer();
            if (!inventories.restoreCashout(player, delivery)) return TrialOperationResult.rejected("Payout is safely pending recovery.");
            if (fameAward > 0) {
                long total = new com.cosmicpve.economy.FameService().balance(player);
                player.sendSystemMessage(Component.literal("+" + fameAward + " FAME")
                        .withStyle(style -> style.withColor(com.cosmicpve.economy.FameService.COLOR).withBold(true)));
                player.sendSystemMessage(Component.literal("Total Fame: ").withColor(0xAAAAAA)
                        .append(Component.literal(Long.toString(total)).withStyle(style -> style
                                .withColor(com.cosmicpve.economy.FameService.COLOR).withBold(true))));
            }
            timerDisplay.hide(player);
            celebrations.schedule(player, session.progress().completedRooms(), server.getTickCount());
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
        TrialSession next = session.withProgress(progress); repository.publish(server, next);
        closeDecisionMenus(server, next);
        resolveIfReady(server, next);
    }

    static void closeDecisionMenus(MinecraftServer server, TrialSession session) {
        forOnline(server, session, player -> {
            if (player.containerMenu instanceof TrialDecisionMenu) player.closeContainer();
        });
    }

    private void resolveIfReady(MinecraftServer server, TrialSession session) {
        if (session.participants().isEmpty()) { cleanupAndClose(server, session); return; }
        if (!session.progress().madness().options().isEmpty()) return;
        boolean ready = allContinuingReady(session);
        if (ready) beginNextRoom(server, session);
    }
    static boolean allContinuingReady(TrialSession session) {
        return !session.participants().isEmpty()
                && session.participants().stream().allMatch(id -> session.progress().decision(id) == TrialDecision.NO_DEAL);
    }

    private void beginNextRoom(MinecraftServer server, TrialSession session) {
        if (session.progress().madness().pending() > 0) {
            beginMadness(server, session); return;
        }
        List<Identifier> pool = productionRoomPool(session.progress().phase());
        Identifier room = selection.select(session, pool, net.minecraft.util.RandomSource.create()).orElse(null);
        if (room == null) {
            CosmicPVE.LOGGER.error("No eligible {} Trial room for session {}", session.progress().phase(), session.sessionId());
            forOnline(server, session, player -> player.sendSystemMessage(Component.literal(
                    "No eligible Trial room is available. The session and pot remain safe in the Decision Box.")));
            return;
        }
        beginRoom(server, session, room);
    }

    private void beginMadness(MinecraftServer server, TrialSession session) {
        var definitions = List.copyOf(CosmicContent.repository().snapshot().madnessDefinitions().values());
        var state = session.progress().madness();
        var random = net.minecraft.util.RandomSource.create();
        while (state.pending() > 0) {
            state = state.offer(definitions, session.progress().portalModifiers().madnessChoices(definitions.size()), random);
            if (!state.options().isEmpty()) break;
            state = state.resolve(session.participants(), random); // Exhausted pool: consume without blocking.
        }
        TrialSession next = session.withProgress(session.progress().withMadness(state))
                .withState(TrialLifecycleState.DECISION, session.stateTicksRemaining() > 0 ? session.stateTicksRemaining() : 600,
                        Optional.empty(), false, session.protectedBounds());
        repository.publish(server,next);
        if (state.pending() == 0) { beginNextRoom(server,next); return; }
        forOnline(server,next,this::openMadness);
    }

    private void openMadness(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new com.cosmicpve.trial.madness.MadnessMenu(id,inventory,player),
                Component.literal("Madness").withColor(0x8C1708)));
    }

    private void tickMadness(MinecraftServer server, TrialSession session) {
        if (session.progress().madness().allSubmitted(session.participants())) {
            resolveMadness(server,session.sessionId(),session.progress().madness().ballotSerial()); return;
        }
        if (server.getTickCount() % 20 == 0) forOnline(server,session,player -> {
            if (session.progress().madness().owesVote(player.getUUID())
                    && !(player.containerMenu instanceof com.cosmicpve.trial.madness.MadnessMenu)) openMadness(player);
        });
        int remaining = Math.max(0,session.stateTicksRemaining()-1);
        if (remaining > 0) { publishTick(server,session.withStateTicks(remaining)); return; }
        resolveMadness(server,session.sessionId(),session.progress().madness().ballotSerial());
    }

    private void resolveMadness(MinecraftServer server,UUID sessionId,long ballotSerial) {
        TrialSession session = active(server).orElse(null);
        if (session == null || !session.sessionId().equals(sessionId) || session.state() != TrialLifecycleState.DECISION) return;
        var before = session.progress().madness();
        var state = before.resolveBallot(ballotSerial,session.participants(),net.minecraft.util.RandomSource.create());
        if (state == before) return;
        TrialSession next = session.withProgress(session.progress().withMadness(state)); repository.publish(server,next);
        if (state.active().size() > before.active().size()) {
            var winner = state.active().getLast();
            forOnline(server,next,player -> player.sendSystemMessage(Component.literal(
                    "Madness Modifier Selected: " + winner.name()).withColor(0x8C1708)));
        }
        forOnline(server,next,ServerPlayer::closeContainer);
        if (state.pending() > 0) next = next.withStateTicks(600); // Existing sequential ballots each receive their own timeout.
        beginNextRoom(server,next);
    }

    public void voteMadness(ServerPlayer player, UUID sessionId, long ballotSerial, List<Identifier> ballot, Identifier option) {
        var server = player.level().getServer(); var session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION || !session.sessionId().equals(sessionId)
                || !session.activeParticipant(player.getUUID())) return;
        var madness = session.progress().madness();
        if (madness.ballotSerial() != ballotSerial || madness.hasSubmittedVote(player.getUUID())
                || ballot.isEmpty() || !ballot.contains(option)
                || !madness.options().stream().map(com.cosmicpve.trial.madness.MadnessDefinition::id).toList().equals(ballot)) return;
        repository.publish(server,session.withProgress(session.progress().withMadness(session.progress().madness().vote(player.getUUID(),option))));
        player.closeContainer();
        var submitted = active(server).orElseThrow();
        if (submitted.progress().madness().allSubmitted(submitted.participants())) resolveMadness(server,sessionId,ballotSerial);
    }

    public TrialOperationResult debugAddMadness(MinecraftServer server, Identifier id) {
        var session = active(server).orElse(null);
        var definition = CosmicContent.repository().snapshot().madnessDefinitions().get(id);
        if (session == null || definition == null || !definition.enabled())
            return TrialOperationResult.rejected("No active Trial or unknown/disabled Madness ID: " + id);
        var state = session.progress().madness();
        if (!state.options().isEmpty()) return TrialOperationResult.rejected("Finish the current Madness ballot first.");
        if (state.active().stream().anyMatch(d -> d.id().equals(id))) return TrialOperationResult.rejected("Madness is already active.");
        var selected = new java.util.ArrayList<>(state.active()); selected.add(definition);
        repository.publish(server,session.withProgress(session.progress().withMadness(
                new com.cosmicpve.trial.madness.MadnessState(state.pending(),selected,List.of(),List.of(),state.ballotSerial()))));
        return TrialOperationResult.ok("Added Trial-local Madness: " + definition.name());
    }

    static List<Identifier> roomPool(TrialPhase phase) {
        var pool = new java.util.ArrayList<Identifier>(APPRENTICE_NATIVE_ROOMS);
        if (phase != TrialPhase.APPRENTICE) pool.addAll(HARDCORE_NATIVE_ROOMS);
        if (phase == TrialPhase.IMPOSSIBLE || phase == TrialPhase.DEMONIC) pool.addAll(IMPOSSIBLE_NATIVE_ROOMS);
        if (phase == TrialPhase.DEMONIC) pool.addAll(DEMONIC_NATIVE_ROOMS);
        return List.copyOf(pool);
    }

    private List<Identifier> productionRoomPool(TrialPhase phase) {
        return CosmicContent.repository().snapshot().trialRooms().values().stream()
                .filter(definition -> definition.enabled() && eligibleNativeTier(definition.category(),phase))
                .filter(definition -> {
                    boolean known = handlers.hasStarter(definition.handler()) && handlers.contains(definition.loadout())
                            && definition.id().equals(definition.handler()); // Bespoke callbacks own their stable room identity.
                    if (!known) CosmicPVE.LOGGER.warn("Ineligible Trial room {}: unknown/incompatible handler or loadout {} / {}",
                            definition.id(),definition.handler(),definition.loadout());
                    return known;
                }).map(com.cosmicpve.content.definition.trial.TrialRoomDefinition::id)
                .sorted(java.util.Comparator.comparing(Identifier::toString)).toList();
    }

    static boolean eligibleNativeTier(com.cosmicpve.content.definition.trial.TrialRoomCategory category,TrialPhase phase) {
        return switch (category) {
            case APPRENTICE -> true;
            case HARDCORE -> phase.ordinal() >= TrialPhase.HARDCORE.ordinal();
            case IMPOSSIBLE -> phase.ordinal() >= TrialPhase.IMPOSSIBLE.ordinal();
            case DEMONIC -> phase == TrialPhase.DEMONIC;
            default -> false;
        };
    }

    private void beginRoom(MinecraftServer server, TrialSession session, Identifier room) {
        worldTimeAtRoomStart(room).ifPresent(time ->
                server.getAllLevels().forEach(world -> world.setDayTime(time)));
        removePortal(server, session); ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        var placed = structures.place(level, CosmicContent.repository().requireTrialRoom(room), ROOM_ORIGIN);
        var start = handlers.initialize(CosmicContent.repository().requireTrialRoom(room).handler(),level,session,placed);
        BlockPos participantSpawn = start.spawn();
        TrialEncounterState encounter = start.encounter();
        roomSpawns.put(session.sessionId(), participantSpawn);
        TrialProgress progress = session.progress().beginRoom(room, encounter);
        InstanceBounds decisionBounds = InstanceBounds.from(CosmicContent.repository().requireTrialRoom(DECISION_ROOM).bounds().at(DECISION_ORIGIN));
        var bounds = List.of(decisionBounds, placed.bounds());
        TrialSession next = session.withProgress(progress).withState(TrialLifecycleState.ROOM_INTRO,
                TrialSession.ROOM_INTRO_TICKS, Optional.of(room), false, bounds);
        repository.publish(server, next);
        forOnline(server, next, player -> {
            handlers.apply(CosmicContent.repository().requireTrialRoom(room).loadout(),player);
            teleport(player, roomSpawns.get(session.sessionId()));
        });
    }

    static java.util.OptionalLong worldTimeAtRoomStart(Identifier room) {
        return HIDDEN_GRAVEYARD.equals(room)
                ? java.util.OptionalLong.of(HIDDEN_GRAVEYARD_WORLD_TIME)
                : java.util.OptionalLong.empty();
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
                + result.nextState().sequenceProgress() + "/6!");
        TrialSession next = session.withProgress(session.progress().withEncounter(result.nextState()));
        repository.publish(level.getServer(), next);
        if (result.complete()) completeProductionRoom(level.getServer());
    }

    public void onUndeadCorpseDeath(UndeadCorpseEntity corpse) {
        if (!(corpse.level() instanceof ServerLevel level)) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(HIDDEN_GRAVEYARD::equals).isEmpty()) return;
        var result = hiddenGraveyard.onDeath(level, session, corpse);
        if (!result.accepted()) return;
        TrialSession next = session.withProgress(session.progress().withEncounter(result.encounter()));
        repository.publish(level.getServer(), next);
        if (result.complete()) completeProductionRoom(level.getServer());
    }

    public void onWarzoneGiantDeath(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level) || !WarzoneGiantsService.encounterGiant(zombie)) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(WARZONE_GIANTS::equals).isEmpty()) return;
        var result = warzone.onDeath(zombie);
        if (result.accepted() && result.complete()) completeProductionRoom(level.getServer());
    }

    public void onInventorDeath(InventorEntity boss) {
        if (!(boss.level() instanceof ServerLevel level) || !inventor.bossDeath(boss)) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(INVENTOR::equals).isEmpty()) return;
        completeProductionRoom(level.getServer());
    }

    public void onCinderWolfDeath(CinderWolfEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.currentRoom().filter(CINDER_WOLF::equals).isPresent())
            cinderWolf.bossDeath(level, session, boss);
    }

    public boolean onCinderWolfProjectileImpact(ServerLevel level, net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball ball) {
        if (!ball.getTags().contains(CinderWolfService.PROJECTILE_TAG)) return false;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(CINDER_WOLF::equals).isEmpty()) {
            ball.discard(); return true;
        }
        return cinderWolf.projectileImpact(level, ball, session);
    }

    public boolean allowsWarzoneGiantDamage(Zombie zombie) {
        if (!WarzoneGiantsService.encounterGiant(zombie)) return true;
        TrialSession session = active(zombie.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.currentRoom().filter(WARZONE_GIANTS::equals).isPresent() && warzone.damageAllowed(zombie);
    }

    public boolean recoverWarzoneGiantFromSuffocation(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level) || !WarzoneGiantsService.encounterGiant(zombie)) return false;
        TrialSession session = active(level.getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.currentRoom().filter(WARZONE_GIANTS::equals).isPresent()
                && warzone.recoverFromSuffocation(level, session, zombie);
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

    public boolean onDeadeyeTarget(ServerPlayer player, BlockPos target) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || !session.activeParticipant(player.getUUID())
                || session.currentRoom().filter(DEADEYE::equals).isEmpty()) return false;
        var result = deadeye.hitTarget((ServerLevel) player.level(), session, target);
        if (!result.accepted()) return false;
        TrialSession next = session.withProgress(session.progress().withEncounter(result.state()));
        repository.publish(player.level().getServer(), next);
        forOnline(player.level().getServer(), next, member -> {
            member.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.0F);
            member.sendSystemMessage(Component.literal("Deadeye " + result.section().name().toLowerCase(java.util.Locale.ROOT)
                    .replace('_', ' ') + " section revealed (" + result.state().sequenceProgress() + "/"
                    + DeadeyeService.SECTION_COUNT + ")."));
        });
        return true;
    }

    public boolean allowsCircuitPlacement(ServerPlayer player, BlockPos pos, BlockState state) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(CIRCUIT_CIRCUS::equals).isEmpty()) return false;
        return CircuitPlacementPolicy.allows(session.activeParticipant(player.getUUID()), true, session.state(),
                roomBounds(session), pos, state);
    }

    public boolean allowsCircuitPlacementUse(ServerPlayer player, BlockPos placementPos, ItemStack held) {
        if (!(held.getItem() instanceof BlockItem blockItem)) return false;
        return allowsCircuitPlacement(player, placementPos, blockItem.getBlock().defaultBlockState());
    }

    public boolean allowsCaveDivingPlacement(ServerPlayer player, BlockPos pos, ItemStack held) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(CAVE_DIVING::equals).isPresent()
                && caveDiving.allowsSolutionPlacement(session, pos, held);
    }

    public boolean rejectsCaveDivingPotPlacement(ServerPlayer player, BlockPos pos, ItemStack held) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(CAVE_DIVING::equals).isPresent()
                && caveDiving.rejectsPotPlacement(session, pos, held);
    }

    public boolean allowsCaveDivingBreak(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(CAVE_DIVING::equals).isPresent()
                && caveDiving.allowsBreak(session, pos);
    }

    public void onCaveDivingDrops(net.neoforged.neoforge.event.level.BlockDropsEvent event) {
        TrialSession session = active(event.getLevel().getServer()).orElse(null);
        if (session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.currentRoom().filter(CAVE_DIVING::equals).isPresent())
            caveDiving.replaceUnderwaterDrops(session, event);
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

    public boolean allowsFireColonyUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(FIRE_COLONY::equals).isPresent()
                && fireColony.isFinalLever(pos, ROOM_ORIGIN);
    }

    public boolean allowsProtectedRoomUse(ServerPlayer player, BlockPos pos) {
        return allowsCircuitUse(player, pos) || allowsFireColonyUse(player, pos) || allowsColdSnapUse(player, pos)
                || allowsHiddenGraveyardUse(player, pos) || allowsDeadeyeUse(player, pos)
                || allowsCaveDivingUse(player, pos) || allowsInventorUse(player, pos);
    }

    public boolean allowsInventorUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || !session.activeParticipant(player.getUUID()) || session.currentRoom().filter(INVENTOR::equals).isEmpty()) return false;
        return java.util.Arrays.stream(InventorService.Station.values())
                .anyMatch(station -> pos.equals(ROOM_ORIGIN.offset(station.controlLocal)));
    }

    public void onInventorControl(ServerPlayer player, BlockPos pos) {
        if (allowsInventorUse(player, pos)) inventor.interact(player, pos);
    }

    public void onInventorPlate(ServerLevel level, BlockPos pos, BlockState state) {
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(INVENTOR::equals).isEmpty()) return;
        inventor.plate(level, session, pos, state);
    }

    public boolean allowsCaveDivingUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(CAVE_DIVING::equals).isPresent()
                && caveDiving.allowsCraftingTableUse(session, (ServerLevel) player.level(), pos);
    }

    public boolean allowsDeadeyeUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(DEADEYE::equals).isPresent()
                && deadeye.isFinalLever(session.sessionId(), pos);
    }

    public boolean allowsHiddenGraveyardUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(HIDDEN_GRAVEYARD::equals).isPresent()
                && hiddenGraveyard.activeChest(session.sessionId(), pos);
    }

    public boolean allowsColdSnapUse(ServerPlayer player, BlockPos pos) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.activeParticipant(player.getUUID())
                && session.currentRoom().filter(COLD_SNAP::equals).isPresent()
                && coldSnap.isFinalLever(pos, ROOM_ORIGIN);
    }

    public void onCircuitLever(ServerPlayer player, BlockPos pos) {
        if (!allowsCircuitUse(player, pos)) return;
        TrialSession session = active(player.level().getServer()).orElseThrow();
        if (circuit.allComplete(session.progress().encounter())) completeProductionRoom(player.level().getServer());
        else player.sendSystemMessage(Component.literal("All four circuits must be complete before using the lever."));
    }

    public void onFireColonyLever(ServerPlayer player, BlockPos pos) {
        if (allowsFireColonyUse(player, pos)) completeProductionRoom(player.level().getServer());
    }

    public void onColdSnapLever(ServerPlayer player, BlockPos pos) {
        if (allowsColdSnapUse(player, pos)) completeProductionRoom(player.level().getServer());
    }

    public void onDeadeyeLever(ServerPlayer player, BlockPos pos) {
        if (!allowsDeadeyeUse(player, pos)) return;
        TrialSession session = active(player.level().getServer()).orElseThrow();
        if (deadeye.canComplete(session.sessionId(), pos, session.progress().encounter()))
            completeProductionRoom(player.level().getServer());
        else player.sendSystemMessage(Component.literal("Reveal all Deadeye sections before using the final lever."));
    }

    public void onColdSnapPlate(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER)
                || state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) <= 0) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(COLD_SNAP::equals).isEmpty()) return;
        boolean gold = pos.equals(ROOM_ORIGIN.offset(ColdSnapService.GOLD_PLATE_LOCAL));
        boolean iron = pos.equals(ROOM_ORIGIN.offset(ColdSnapService.IRON_PLATE_LOCAL));
        if (!gold && !iron) return;
        boolean participantPresent = level.getEntitiesOfClass(ServerPlayer.class,
                new net.minecraft.world.phys.AABB(pos).inflate(0.1D), player -> canActivateColdSnap(session, player.getUUID())).stream().findAny().isPresent();
        if (!participantPresent) return;
        var result = gold ? coldSnap.activateGold(level, session, ROOM_ORIGIN) : coldSnap.activateIron(level, session);
        if (result.accepted()) repository.publish(level.getServer(), session.withProgress(session.progress().withEncounter(result.state())));
    }

    public void onBombSquadPlate(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER)
                || state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) <= 0) return;
        TrialSession session = active(level.getServer()).orElse(null);
        if (!activeBombSquad(session)) return;
        var participants = level.getEntitiesOfClass(ServerPlayer.class, new net.minecraft.world.phys.AABB(pos).inflate(0.1D),
                player -> session.activeParticipant(player.getUUID()));
        if (participants.isEmpty()) return;
        if (bombSquad.activeExit(session, pos)) {
            completeProductionRoom(level.getServer()); return;
        }
        for (ServerPlayer player : participants) bombSquad.dispenseEgg(player, session, pos);
    }

    public boolean allowsBombSquadEggUse(ServerPlayer player, BlockPos pos, ItemStack held) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        return activeBombSquad(session) && session.activeParticipant(player.getUUID())
                && roomBounds(session).contains(pos) && BombSquadService.issuedEgg(held);
    }

    public boolean onBombSquadCreeperJoin(ServerLevel level, Creeper creeper) {
        if (!BombSquadService.encounterCreeper(creeper)) return true;
        TrialSession session = active(level.getServer()).orElse(null);
        return activeBombSquad(session) && bombSquad.acceptEncounterCreeper(level, session, creeper);
    }

    public boolean allowsBombSquadExplosion(ServerLevel level, ServerExplosion explosion, BlockPos pos) {
        TrialSession session = active(level.getServer()).orElse(null);
        Entity source = explosion.getDirectSourceEntity();
        return activeBombSquad(session) && bombSquad.allowsExplosionBlock(level, session, source, pos);
    }

    public boolean allowsBombSquadMobGrief(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        TrialSession session = active(level.getServer()).orElse(null);
        return activeBombSquad(session) && bombSquad.trackedEncounterCreeper(session, entity);
    }

    private static boolean activeBombSquad(TrialSession session) {
        return session != null && session.state() == TrialLifecycleState.ROOM_ACTIVE
                && session.currentRoom().filter(BOMB_SQUAD::equals).isPresent();
    }

    public String bombSquadStatus(UUID sessionId) { return bombSquad.status(sessionId); }
    public String hiddenGraveyardStatus(UUID sessionId) { return hiddenGraveyard.status(sessionId); }
    public String deadeyeStatus(TrialSession session) {
        var fallThreshold = deadeye.fallThreshold(session.sessionId());
        String threshold = fallThreshold.isPresent() ? Integer.toString(fallThreshold.getAsInt()) : "unavailable";
        return "Deadeye section=" + session.progress().encounter().sequenceProgress() + "/"
                + DeadeyeService.SECTION_COUNT + " fallY=" + threshold
                + " participants=" + session.participants().size();
    }
    public String hazeStatus(UUID sessionId) { return haze.status(sessionId); }
    public String warzoneStatus(UUID sessionId) { return warzone.status(sessionId); }
    public String inventorStatus(MinecraftServer server, UUID sessionId) {
        ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        return level == null ? "Inventor level unavailable" : inventor.status(level, sessionId);
    }
    public TrialOperationResult debugActivateInventor(MinecraftServer server) {
        TrialSession session = active(server).orElse(null);
        ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (session == null || level == null || session.state() != TrialLifecycleState.ROOM_ACTIVE
                || session.currentRoom().filter(INVENTOR::equals).isEmpty())
            return TrialOperationResult.rejected("Inventor is not active.");
        return inventor.forceActivation(level, session)
                ? TrialOperationResult.ok("Activated one inactive Inventor workstation.")
                : TrialOperationResult.rejected("All Inventor workstations are already active.");
    }
    public String caveDivingStatus(TrialSession session, ServerLevel level) {
        return caveDiving.status(session.sessionId(), level);
    }
    public String productionPoolStatus(TrialSession session) {
        return productionRoomPool(session.progress().phase()).stream()
                .map(room -> room.getPath().substring(room.getPath().lastIndexOf('/') + 1) + "=" + selection.weight(session, room))
                .collect(java.util.stream.Collectors.joining(", "));
    }
    public TrialPerformanceTracker.Snapshot performanceSnapshot() { return performance.snapshot(); }

    static boolean canActivateColdSnap(TrialSession session, UUID playerId) {
        return session.state() == TrialLifecycleState.ROOM_ACTIVE && session.activeParticipant(playerId)
                && session.currentRoom().filter(COLD_SNAP::equals).isPresent();
    }

    private ZeroGTickResult tickZeroGObjectives(MinecraftServer server, ServerLevel level, TrialSession session) {
        TrialSession current = session;
        for (UUID participant : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(participant);
            if (player == null) continue;
            Optional<BlockPos> objective = zeroG.objectiveAt(player.blockPosition(), ROOM_ORIGIN, current.progress().encounter());
            if (objective.isEmpty()) continue;
            var activation = zeroG.activatePlate(level, player, objective.orElseThrow(),
                    current.progress().encounter(), net.minecraft.util.RandomSource.create());
            if (!activation.accepted()) continue;
            current = withZeroGEncounter(current, activation.state());
            int completed = current.progress().encounter().completedObjectives().size();
            broadcast(server, current, "Zero-G Objective " + completed + "/10!");
            if (activation.complete()) {
                repository.publish(server, current); // completion reads the exact authoritative ten-position set
                completeProductionRoom(server);
                return new ZeroGTickResult(current, true);
            }
        }
        return new ZeroGTickResult(current, false);
    }

    record ZeroGTickResult(TrialSession session, boolean completed) {}
    public static TrialSession withZeroGEncounter(TrialSession session, TrialEncounterState encounter) {
        return session.withProgress(session.progress().withEncounter(encounter));
    }

    public TrialOperationResult exit(ServerPlayer player) {
        TrialSession session = active(player.level().getServer()).orElse(null);
        if (session == null || !session.activeParticipant(player.getUUID())) return TrialOperationResult.rejected("You are not an active Trial participant.");
        clearInventorCharge(player.level().getServer(), session, player.getUUID());
        loadouts.clear(player); if (!inventories.restore(player)) return TrialOperationResult.rejected("Your outside snapshot could not be restored safely.");
        timerDisplay.hide(player);
        TrialSession next = session.removeParticipant(player.getUUID());
        if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        return TrialOperationResult.ok("Exited the Trial and restored your outside state.");
    }

    public TrialOperationResult abort(MinecraftServer server, String reason) {
        TrialSession session = active(server).orElse(null); if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        forOnline(server, session, player -> { loadouts.clear(player); inventories.restore(player); timerDisplay.hide(player); }); cleanupAndClose(server, session);
        return TrialOperationResult.ok(reason);
    }
    private void failForTimeout(MinecraftServer server, TrialSession session) {
        int level = session.progress().portalModifiers().insuranceLevel();
        forOnline(server, session, player -> {
            loadouts.clear(player);
            if (level > 0) {
                prepareInsurance(player, session, net.minecraft.util.RandomSource.create());
                inventories.restoreCashout(player, delivery);
            } else inventories.restore(player);
            timerDisplay.hide(player);
        });
        cleanupAndClose(server, session);
    }
    public TrialOperationResult adjustTimer(MinecraftServer server, int ticks, boolean absolute) {
        TrialSession session = active(server).orElse(null); if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        long candidate = absolute ? ticks : (long) session.timerTicks() + ticks;
        int bounded = (int) Math.max(0, Math.min(Integer.MAX_VALUE, candidate)); repository.publish(server, session.withTimer(bounded));
        return TrialOperationResult.ok("Trial timer is now " + bounded + " ticks.");
    }
    public void onDisconnect(ServerPlayer player) {
        celebrations.cancel(player.getUUID());
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            if (session.currentRoom().filter(HAZE_AND_SEEK::equals).isPresent()) haze.removeParticipant(player, session.sessionId());
            clearInventorCharge(player.level().getServer(), session, player.getUUID());
            timerDisplay.hide(player);
            TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        });
    }
    public void onLogin(ServerPlayer player) {
        boolean active = active(player.level().getServer()).map(session -> session.activeParticipant(player.getUUID())).orElse(false);
        if (!active) {
            timerDisplay.hide(player);
            if (inventories.pending(player).isPresent()) inventories.recover(player, delivery);
        }
    }
    public void onDeath(ServerPlayer player) {
        active(player.level().getServer()).filter(session -> session.activeParticipant(player.getUUID())).ifPresent(session -> {
            if (session.currentRoom().filter(HAZE_AND_SEEK::equals).isPresent()) haze.removeParticipant(player, session.sessionId());
            clearInventorCharge(player.level().getServer(), session, player.getUUID());
            int level = session.progress().portalModifiers().insuranceLevel();
            if (level > 0 && !prepareInsurance(player, session, net.minecraft.util.RandomSource.create()))
                CosmicPVE.LOGGER.error("Could not durably prepare insured Trial recovery for {}", player.getUUID());
            timerDisplay.hide(player); inventories.clearTrialInventory(player); TrialSession next = session.removeParticipant(player.getUUID());
            if (next.participants().isEmpty()) cleanupAndClose(player.level().getServer(), next); else repository.publish(player.level().getServer(), next);
        });
    }
    public void onRespawn(ServerPlayer player) { if (inventories.pending(player).isPresent()) inventories.recover(player, delivery); }
    public boolean emergencyRestore(ServerPlayer player) { return inventories.recover(player, delivery); }
    boolean prepareInsurance(ServerPlayer player, TrialSession session, net.minecraft.util.RandomSource random) {
        int level = session.progress().portalModifiers().insuranceLevel();
        if (level <= 0) return true;
        var selected = insurance.select(session.progress().pot(), level, random);
        return inventories.prepareRewardedRestore(player, insurance.flattenedCopies(selected));
    }
    public void debugSound(ServerPlayer player, boolean roomStart) {
        if (roomStart) titles.debugRoomStartSound(player); else titles.debugCountdownSound(player);
    }
    public TrialOperationResult debugSetCompletedRooms(MinecraftServer server, int rooms) {
        TrialSession session = active(server).orElse(null);
        if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        repository.publish(server, session.withProgress(session.progress().debugSetCompletedRooms(rooms)));
        return TrialOperationResult.ok("Trial completed-room count set to " + rooms + " for development testing.");
    }
    public TrialOperationResult debugFillPot(MinecraftServer server, int count) {
        TrialSession session = active(server).orElse(null);
        if (session == null) return TrialOperationResult.rejected("No active Trial exists.");
        repository.publish(server, session.withProgress(session.progress().debugFillPot(count)));
        forOnline(server, session, player -> {
            if (player.containerMenu instanceof TrialDecisionMenu menu) menu.refresh();
        });
        return TrialOperationResult.ok("Trial pot filled with " + count + " development entries.");
    }
    public TrialOperationResult debugForceRoom(MinecraftServer server, Identifier room) {
        TrialSession session = active(server).orElse(null);
        if (session == null || session.state() != TrialLifecycleState.DECISION)
            return TrialOperationResult.rejected("Force-room requires an active Decision Box.");
        if (!APPRENTICE_NATIVE_ROOMS.contains(room) && !HARDCORE_NATIVE_ROOMS.contains(room)
                && !IMPOSSIBLE_NATIVE_ROOMS.contains(room) && !DEMONIC_NATIVE_ROOMS.contains(room))
            return TrialOperationResult.rejected("Unknown production Trial room: " + room);
        TrialPhase phase = DEMONIC_NATIVE_ROOMS.contains(room) ? TrialPhase.DEMONIC
                : IMPOSSIBLE_NATIVE_ROOMS.contains(room) ? TrialPhase.IMPOSSIBLE
                : HARDCORE_NATIVE_ROOMS.contains(room) ? TrialPhase.HARDCORE : TrialPhase.APPRENTICE;
        TrialPhase activePhase = session.progress().phase();
        TrialPhase selectedPhase = activePhase.ordinal() >= phase.ordinal() ? activePhase : phase;
        TrialSession prepared = session.withProgress(session.progress().debugEnterPhase(selectedPhase));
        beginRoom(server, prepared, room);
        return TrialOperationResult.ok("Forced Trial room " + room + ".");
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
        timerDisplay.hideAll(server); cleanupWorldState(server, session); repository.clear(server); decisionSpawns.remove(session.sessionId());
        roomSpawns.remove(session.sessionId()); protection.clearExplicitAllows();
    }
    private void cleanupWorldState(MinecraftServer server, TrialSession session) {
        removePortal(server, session); ServerLevel instance = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (instance != null) {
            if (session.currentRoom().filter(ZERO_G::equals).isPresent()) zeroG.cleanup(instance, session, roomBounds(session));
            if (session.currentRoom().filter(BOMB_SQUAD::equals).isPresent()) bombSquad.cleanup(instance, session.sessionId());
            if (session.currentRoom().filter(HIDDEN_GRAVEYARD::equals).isPresent()) hiddenGraveyard.cleanup(instance, session.sessionId());
            if (session.currentRoom().filter(DEADEYE::equals).isPresent()) deadeye.cleanup(session.sessionId());
            if (session.currentRoom().filter(HAZE_AND_SEEK::equals).isPresent()) haze.cleanup(instance, session);
            if (session.currentRoom().filter(WARZONE_GIANTS::equals).isPresent()) warzone.cleanup(instance, session.sessionId());
            if (session.currentRoom().filter(CAVE_DIVING::equals).isPresent()) caveDiving.cleanup(session.sessionId());
            if (session.currentRoom().filter(INVENTOR::equals).isPresent()) inventor.cleanup(instance, session.sessionId());
            if (session.currentRoom().filter(CINDER_WOLF::equals).isPresent()) cinderWolf.cleanup(instance, session.sessionId());
            fireColony.cleanup(session.sessionId()); coldSnap.cleanup(session.sessionId());
            session.protectedBounds().forEach(bounds -> structures.cleanup(instance, bounds));
        }
    }
    private void cleanupCurrentRoom(ServerLevel level, TrialSession session) {
        if (level == null || session.currentRoom().isEmpty()) return;
        if (session.currentRoom().filter(ZERO_G::equals).isPresent()) zeroG.cleanup(level, session, roomBounds(session));
        if (session.currentRoom().filter(FIRE_COLONY::equals).isPresent()) fireColony.cleanup(session.sessionId());
        if (session.currentRoom().filter(COLD_SNAP::equals).isPresent()) coldSnap.cleanup(session.sessionId());
        if (session.currentRoom().filter(BOMB_SQUAD::equals).isPresent()) bombSquad.cleanup(level, session.sessionId());
        if (session.currentRoom().filter(HIDDEN_GRAVEYARD::equals).isPresent()) hiddenGraveyard.cleanup(level, session.sessionId());
        if (session.currentRoom().filter(DEADEYE::equals).isPresent()) deadeye.cleanup(session.sessionId());
        if (session.currentRoom().filter(HAZE_AND_SEEK::equals).isPresent()) haze.cleanup(level, session);
        if (session.currentRoom().filter(WARZONE_GIANTS::equals).isPresent()) warzone.cleanup(level, session.sessionId());
        if (session.currentRoom().filter(CAVE_DIVING::equals).isPresent()) caveDiving.cleanup(session.sessionId());
        if (session.currentRoom().filter(INVENTOR::equals).isPresent()) inventor.cleanup(level, session.sessionId());
        if (session.currentRoom().filter(CINDER_WOLF::equals).isPresent()) cinderWolf.cleanup(level, session.sessionId());
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
        for (UUID id : session.participants()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) action.accept(player);
        }
    }
    private static void broadcast(MinecraftServer server, TrialSession session, String message) {
        forOnline(server, session, player -> player.sendSystemMessage(Component.literal(message)));
    }
    private void publishTick(MinecraftServer server, TrialSession session) {
        repository.publishVolatile(server, session); if (server.getTickCount() % 100 == 0) repository.flush(server);
    }
    private void clearInventorCharge(MinecraftServer server, TrialSession session, UUID player) {
        if (session.currentRoom().filter(INVENTOR::equals).isEmpty()) return;
        ServerLevel level = server.getLevel(TrialRuntime.INSTANCE_DIMENSION);
        if (level != null) inventor.removeParticipantCharge(level, session.sessionId(), player);
    }
}
