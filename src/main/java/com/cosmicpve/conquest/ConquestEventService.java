package com.cosmicpve.conquest;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.entity.spacepirate.SpacePirateEntity;
import com.cosmicpve.registry.ModBlocks;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import com.cosmicpve.content.CosmicContent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

public final class ConquestEventService {
    public static final Identifier REWARD_TABLE = CosmicPVE.id("conquest");
    public static final int NATURAL_COORDINATE_LIMIT = 5_000;
    public static final int FLARE_RADIUS = 100;
    public static final int PLACEMENT_ATTEMPTS = 32;
    public static final int NATURAL_LIFETIME_TICKS = 30 * 60 * 20;
    public static final int ANNOUNCEMENT_INTERVAL_TICKS = 5 * 60 * 20;
    public static final int SCHEDULER_INTERVAL_TICKS = 100;
    public static final long MIN_BANKNOTE_CENTS = 10_000_000L;
    public static final long BANKNOTE_STEP_CENTS = 1_000_000L;
    public static final long MAX_BANKNOTE_CENTS = 100_000_000L;
    public static final int REWARD_ROLLS = 3;

    private final ConquestRepository repository;
    private final RewardDeliveryService delivery = new RewardDeliveryService();

    public ConquestEventService(ConquestRepository repository) { this.repository = repository; }

    public List<ConquestEvent> active(MinecraftServer server) { return repository.active(server); }
    public Optional<ConquestEvent> find(MinecraftServer server, UUID id) { return repository.find(server, id); }
    public Optional<ConquestEvent> at(MinecraftServer server, BlockPos pos) { return repository.at(server, pos); }
    public boolean protectedPosition(MinecraftServer server, BlockPos pos) {
        return repository.active(server).stream().anyMatch(event -> event.bounds().contains(pos));
    }
    public boolean deniesMutation(ServerLevel level, net.minecraft.world.entity.player.Player actor,
            BlockPos pos, boolean breaking) {
        if (!level.dimension().equals(Level.OVERWORLD) || actor != null && actor.isCreative()) return false;
        Optional<ConquestEvent> event = repository.active(level.getServer()).stream()
                .filter(candidate -> candidate.bounds().contains(pos)).findFirst();
        if (event.isEmpty()) return false;
        if (breaking && event.orElseThrow().chestPosition().equals(pos)
                && !event.orElseThrow().interacted()) return true;
        return shouldDeny(true, true, actor != null && actor.isCreative(), breaking,
                event.orElseThrow().chestPosition().equals(pos));
    }

    public void tick(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null || level.getGameTime() % SCHEDULER_INTERVAL_TICKS != 0) return;
        recoverCommittedRewards(server);
        processActive(level);
        scheduleNatural(level);
    }

    private void recoverCommittedRewards(MinecraftServer server) {
        for (ConquestEvent event : repository.all(server)) {
            if (event.state() != ConquestEventState.COMPLETED || event.completionPlayer().isEmpty()) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(event.completionPlayer().orElseThrow());
            if (player == null) continue;
            delivery.deliver(player, event.committedRewards());
            repository.remove(server, event.id());
            repository.flush(server);
        }
    }

    private void processActive(ServerLevel level) {
        long now = level.getGameTime();
        for (ConquestEvent event : repository.active(level.getServer())) {
            if (event.origin() == ConquestOrigin.NATURAL && !event.interacted()) {
                long age = Math.max(0L, now - event.createdGameTime());
                if (shouldExpire(event.origin(), event.interacted(), age)) {
                    expire(level, event.id());
                    continue;
                }
                if (now - event.lastAnnouncementGameTime() >= ANNOUNCEMENT_INTERVAL_TICKS) {
                    announce(level.getServer(), event, remainingMinutes(age));
                    repository.publish(level.getServer(), event.announcedAt(now));
                }
            }
            if (level.hasChunkAt(event.chestPosition())
                    && level.getBlockState(event.chestPosition()).isAir()) {
                level.setBlock(event.chestPosition(), ModBlocks.CONQUEST_CHEST.get().defaultBlockState(), 3);
            }
        }
    }

    private void scheduleNatural(ServerLevel level) {
        long trigger = latestSeventhDay(currentDay(level.getDayTime()));
        if (!shouldSchedule(trigger, repository.lastScheduledDay(level.getServer()))) return;
        repository.setLastScheduledDay(level.getServer(), trigger);
        spawnNatural(level, level.getRandom());
        repository.flush(level.getServer());
    }

    public Optional<ConquestEvent> spawnNatural(ServerLevel level, RandomSource random) {
        Optional<BlockPos> position = findNaturalPosition(level, random);
        if (position.isEmpty()) {
            CosmicPVE.LOGGER.warn("Unable to find a valid bounded surface for seventh-day Conquest Chest");
            return Optional.empty();
        }
        return createAt(level, ConquestOrigin.NATURAL, position.orElseThrow(), true);
    }

    public Optional<ConquestEvent> spawnNear(ServerLevel level, ConquestOrigin origin, BlockPos center,
            int radius, RandomSource random, boolean announce) {
        if (!level.dimension().equals(Level.OVERWORLD)) return Optional.empty();
        Optional<BlockPos> position = findPosition(level, center.getX() - radius, center.getX() + radius,
                center.getZ() - radius, center.getZ() + radius, random, PLACEMENT_ATTEMPTS);
        return position.flatMap(pos -> createAt(level, origin, pos, announce));
    }

    /** Deterministic diagnostic search which still uses the production placement and persistence transaction. */
    public Optional<ConquestEvent> spawnNearest(ServerLevel level, BlockPos center, int radius) {
        if (!level.dimension().equals(Level.OVERWORLD)) return Optional.empty();
        int attempts = 0;
        for (int distance = 0; distance <= radius && attempts < PLACEMENT_ATTEMPTS; distance++) {
            for (int deltaX = -distance; deltaX <= distance && attempts < PLACEMENT_ATTEMPTS; deltaX++) {
                for (int deltaZ = -distance; deltaZ <= distance && attempts < PLACEMENT_ATTEMPTS; deltaZ++) {
                    if (Math.max(Math.abs(deltaX), Math.abs(deltaZ)) != distance) continue;
                    attempts++;
                    BlockPos candidate = resolveSurface(level, center.getX() + deltaX, center.getZ() + deltaZ);
                    if (validSurface(level, candidate) && !overlaps(level.getServer(), candidate))
                        return createAt(level, ConquestOrigin.FLARE, candidate, false);
                }
            }
        }
        CosmicPVE.LOGGER.warn("Conquest spawn-here found no valid surface within {} blocks after {} attempts in {}",
                radius, attempts, level.dimension().identifier());
        return Optional.empty();
    }

    Optional<ConquestEvent> createAt(ServerLevel level, ConquestOrigin origin, BlockPos position, boolean announce) {
        if (!validSurface(level, position) || overlaps(level.getServer(), position)) return Optional.empty();
        ConquestEvent event = ConquestEvent.create(UUID.randomUUID(), origin, position, level.getGameTime());
        BlockState previous = level.getBlockState(position);
        boolean placed = level.setBlock(position, ModBlocks.CONQUEST_CHEST.get().defaultBlockState(), 3);
        BlockState actual = level.getBlockState(position);
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!placed || !actual.is(ModBlocks.CONQUEST_CHEST.get())
                || !(blockEntity instanceof ConquestChestBlockEntity)) {
            if (actual.is(ModBlocks.CONQUEST_CHEST.get())) level.setBlock(position, previous, 3);
            CosmicPVE.LOGGER.warn("Conquest {} placement failed at {}: setBlock={}, actual={}, blockEntity={}",
                    origin, position.toShortString(), placed, actual.getBlock().builtInRegistryHolder().getRegisteredName(),
                    blockEntity == null ? "none" : blockEntity.getClass().getSimpleName());
            return Optional.empty();
        }
        try {
            repository.publish(level.getServer(), event);
            repository.flush(level.getServer());
        } catch (RuntimeException exception) {
            repository.remove(level.getServer(), event.id());
            level.setBlock(position, previous, 3);
            CosmicPVE.LOGGER.error("Rolled back Conquest {} at {} after persistence failed",
                    event.id(), position.toShortString(), exception);
            return Optional.empty();
        }
        CosmicPVE.LOGGER.info("Created {} Conquest event {} at {} in {}; block={}, tracked={}", origin,
                event.id(), position.toShortString(), level.dimension().identifier(),
                level.getBlockState(position).is(ModBlocks.CONQUEST_CHEST.get()),
                repository.find(level.getServer(), event.id()).isPresent());
        if (announce && origin == ConquestOrigin.NATURAL) announce(level.getServer(), event, 30);
        return Optional.of(event);
    }

    public boolean interact(ServerPlayer player, BlockPos position) {
        MinecraftServer server = player.level().getServer();
        Optional<ConquestEvent> existing = repository.at(server, position);
        if (existing.isEmpty() || existing.orElseThrow().interacted()) return false;
        List<SpacePirateEntity> pirates = preparePirates(player.level(), existing.orElseThrow(), player.getRandom());
        if (pirates.size() < 3) {
            player.displayClientMessage(Component.literal(
                    "The Conquest Chest cannot summon its guards here. Clear nearby space and try again."), true);
            return false;
        }
        ConquestEvent discovered = existing.orElseThrow().discovered();
        // Persist the once-only ambush receipt before creating any mobs.
        repository.publish(server, discovered);
        repository.flush(server);
        for (SpacePirateEntity pirate : pirates) {
            if (!player.level().addFreshEntity(pirate))
                CosmicPVE.LOGGER.error("Failed to add prepared Space Pirate {} for Conquest {}",
                        pirate.getUUID(), discovered.id());
        }
        return true;
    }

    public boolean complete(ServerPlayer player, BlockPos position) {
        return complete(player, position, delivery::deliver);
    }

    boolean complete(ServerPlayer player, BlockPos position,
            BiConsumer<ServerPlayer, List<ItemStack>> rewardDelivery) {
        MinecraftServer server = player.level().getServer();
        Optional<ConquestEvent> existing = repository.at(server, position);
        if (existing.isEmpty()) return false;
        ConquestEvent event = existing.orElseThrow();
        if (!event.interacted()) return false;
        var context = new RewardGenerationContext(player.registryAccess(), player.getRandom(), player);
        var rewards = new ArrayList<>(new RewardTableService(CosmicContent.repository(), new RewardGeneratorService())
                .roll(REWARD_TABLE, REWARD_ROLLS, context));
        rewards.add(Banknotes.create(randomBanknoteCents(player.getRandom())));
        // Commit concrete stacks and recipient durably before delivery; recovery is idempotent after restart.
        repository.publish(server, event.committedTo(player.getUUID(), rewards));
        repository.flush(server);
        rewardDelivery.accept(player, rewards);
        repository.remove(server, event.id());
        repository.flush(server);
        return true;
    }

    public boolean expire(ServerLevel level, UUID id) {
        Optional<ConquestEvent> existing = repository.find(level.getServer(), id);
        if (existing.isEmpty() || existing.orElseThrow().state() != ConquestEventState.ACTIVE) return false;
        ConquestEvent event = existing.orElseThrow();
        if (level.getBlockState(event.chestPosition()).is(ModBlocks.CONQUEST_CHEST.get()))
            level.removeBlock(event.chestPosition(), false);
        repository.publish(level.getServer(), event.withState(ConquestEventState.EXPIRED));
        repository.remove(level.getServer(), id);
        repository.flush(level.getServer());
        return true;
    }

    private List<SpacePirateEntity> preparePirates(ServerLevel level, ConquestEvent event, RandomSource random) {
        List<BlockPos> candidates = new ArrayList<>();
        for (int deltaX = -4; deltaX <= 4; deltaX++) {
            for (int deltaZ = -4; deltaZ <= 4; deltaZ++) {
                int x = event.chestPosition().getX() + deltaX;
                int z = event.chestPosition().getZ() + deltaZ;
                BlockPos pos = new BlockPos(x,
                        level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                if (validMobPosition(level, pos)) candidates.add(pos);
            }
        }
        shuffle(candidates, random);
        int requested = Math.min(pirateCount(random), candidates.size());
        List<SpacePirateEntity> pirates = new ArrayList<>(requested);
        for (BlockPos pos : candidates) {
            if (pirates.size() >= requested) break;
            var type = random.nextBoolean() ? ModEntities.SPACE_PIRATE_VARIANT_1.get()
                    : ModEntities.SPACE_PIRATE_VARIANT_2.get();
            SpacePirateEntity pirate = type.create(level, EntitySpawnReason.EVENT);
            if (pirate == null) continue;
            pirate.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            pirate.setYRot(random.nextFloat() * 360F);
            if (!level.noCollision(pirate, pirate.getBoundingBox())) continue;
            pirate.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            pirate.setPersistenceRequired();
            pirates.add(pirate);
        }
        return pirates;
    }

    static <T> void shuffle(List<T> values, RandomSource random) {
        for (int index = values.size() - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            T value = values.get(index);
            values.set(index, values.get(swapIndex));
            values.set(swapIndex, value);
        }
    }

    private Optional<BlockPos> findNaturalPosition(ServerLevel level, RandomSource random) {
        return findPosition(level, -NATURAL_COORDINATE_LIMIT, NATURAL_COORDINATE_LIMIT,
                -NATURAL_COORDINATE_LIMIT, NATURAL_COORDINATE_LIMIT, random, PLACEMENT_ATTEMPTS);
    }

    public Optional<BlockPos> findPosition(ServerLevel level, int minimumX, int maximumX,
            int minimumZ, int maximumZ, RandomSource random, int attempts) {
        int invalidSurface = 0;
        int overlapping = 0;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = random.nextIntBetweenInclusive(minimumX, maximumX);
            int z = random.nextIntBetweenInclusive(minimumZ, maximumZ);
            BlockPos candidate = resolveSurface(level, x, z);
            if (!validSurface(level, candidate)) { invalidSurface++; continue; }
            if (overlaps(level.getServer(), candidate)) { overlapping++; continue; }
            return Optional.of(candidate);
        }
        CosmicPVE.LOGGER.warn("Conquest surface search exhausted {} attempts in {}: invalidSurface={}, overlap={}",
                attempts, level.dimension().identifier(), invalidSurface, overlapping);
        return Optional.empty();
    }

    private static BlockPos resolveSurface(ServerLevel level, int x, int z) {
        // Explicit synchronous access is bounded by PLACEMENT_ATTEMPTS and permits natural events in unvisited chunks.
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos candidate = new BlockPos(x, y, z);
        BlockState below = level.getBlockState(candidate.below());
        if (clearForChest(below) && !below.isFaceSturdy(level, candidate.below(), Direction.UP))
            candidate = candidate.below();
        return candidate;
    }

    public static boolean hasTwoAirFaces(java.util.function.Predicate<BlockPos> isAir, BlockPos position) {
        int count = 0;
        for (Direction direction : Direction.values()) if (isAir.test(position.relative(direction)) && ++count >= 2) return true;
        return false;
    }

    boolean validSurface(ServerLevel level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        BlockPos below = position.below();
        BlockState belowState = level.getBlockState(below);
        int airFaces = 0;
        for (Direction direction : Direction.values())
            if (openFace(level.getBlockState(position.relative(direction)))) airFaces++;
        return validSurfaceSemantics(clearForChest(state), state.getFluidState().isEmpty(),
                belowState.getFluidState().isEmpty(), belowState.isFaceSturdy(level, below, Direction.UP), airFaces);
    }

    static boolean validSurfaceSemantics(boolean clearTarget, boolean dryTarget, boolean dryBelow,
            boolean sturdyBelow, int openFaces) {
        return clearTarget && dryTarget && dryBelow && sturdyBelow && openFaces >= 2;
    }

    private static boolean clearForChest(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean openFace(BlockState state) { return clearForChest(state); }

    private boolean overlaps(MinecraftServer server, BlockPos candidate) {
        ConquestBounds bounds = ConquestBounds.around(candidate);
        return repository.active(server).stream().anyMatch(event -> event.chestPosition().equals(candidate)
                || event.bounds().overlaps(bounds));
    }

    private static boolean validMobPosition(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static void announce(MinecraftServer server, ConquestEvent event, int minutes) {
        Component message = Component.literal("Look alive cosmonaut! A Conquest Chest has spawned at ("
                + event.chestPosition().getX() + ", " + event.chestPosition().getZ()
                + ") and will disappear in " + minutes + " minutes!");
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    public static long currentDay(long dayTime) { return Math.floorDiv(dayTime, 24_000L) + 1L; }
    public static long latestSeventhDay(long currentDay) { return currentDay < 7 ? 0 : (currentDay / 7L) * 7L; }
    public static boolean shouldSchedule(long triggerDay, long lastScheduledDay) {
        return triggerDay > 0 && triggerDay > lastScheduledDay;
    }
    public static boolean shouldExpire(ConquestOrigin origin, boolean interacted, long ageTicks) {
        return origin == ConquestOrigin.NATURAL && !interacted && ageTicks >= NATURAL_LIFETIME_TICKS;
    }
    public static int remainingMinutes(long ageTicks) {
        long remaining = Math.max(0L, NATURAL_LIFETIME_TICKS - ageTicks);
        return (int) Math.ceil(remaining / 1200.0D);
    }
    public static long randomBanknoteCents(RandomSource random) {
        return random.nextIntBetweenInclusive(10, 100) * BANKNOTE_STEP_CENTS;
    }
    public static int pirateCount(RandomSource random) { return random.nextIntBetweenInclusive(3, 5); }
    public static boolean shouldDeny(boolean overworld, boolean insideBounds, boolean creative,
            boolean breaking, boolean conquestChest) {
        return overworld && insideBounds && !creative && !(breaking && conquestChest);
    }
}
