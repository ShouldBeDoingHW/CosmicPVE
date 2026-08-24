package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Event-driven Zero-G objectives plus eight UUID-tracked vanilla Shulker fixtures. */
public final class ZeroGService {
    public static final List<BlockPos> OBJECTIVE_PLATES = List.of(
            new BlockPos(6,6,12), new BlockPos(14,11,6), new BlockPos(8,12,6), new BlockPos(12,14,14),
            new BlockPos(4,19,11), new BlockPos(13,21,7), new BlockPos(11,23,14), new BlockPos(8,29,9),
            new BlockPos(14,32,13), new BlockPos(9,36,16));
    public static final List<BlockPos> SHULKER_FIXTURES = List.of(
            new BlockPos(6,3,2), new BlockPos(2,20,6), new BlockPos(6,7,18), new BlockPos(2,14,14),
            new BlockPos(14,7,2), new BlockPos(18,14,6), new BlockPos(14,3,18), new BlockPos(18,21,14));
    private static final String TAG = "cosmicpve.zero_g_fixture";
    private final Map<UUID, Map<UUID, Vec3>> fixtures = new HashMap<>();

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, InstanceBounds bounds, BlockPos origin) {
        List<Shulker> shulkers = level.getEntitiesOfClass(Shulker.class, box(bounds));
        if (shulkers.size() != SHULKER_FIXTURES.size()) {
            throw new IllegalStateException("Zero-G requires exactly 8 structure Shulkers, found " + shulkers.size());
        }
        Map<UUID, Vec3> expected = new HashMap<>();
        for (BlockPos local : SHULKER_FIXTURES) {
            Vec3 fixture = Vec3.atBottomCenterOf(origin.offset(local));
            Shulker shulker = shulkers.stream().filter(candidate -> !expected.containsKey(candidate.getUUID()))
                    .min(java.util.Comparator.comparingDouble(candidate -> candidate.position().distanceToSqr(fixture)))
                    .orElseThrow();
            configure(shulker, session.sessionId(), true);
            shulker.setPos(fixture.x, fixture.y, fixture.z);
            expected.put(shulker.getUUID(), fixture);
        }
        fixtures.put(session.sessionId(), Map.copyOf(expected));
        return new TrialEncounterState(List.of(), 0, List.of(), List.of(), List.of(), List.of(),
                expected.keySet().stream().toList());
    }

    public void activate(ServerLevel level, TrialSession session) {
        for (UUID id : session.progress().encounter().encounterEntities()) {
            if (level.getEntity(id) instanceof Shulker shulker) configure(shulker, session.sessionId(), false);
        }
    }

    public void keepFixturesPinned(ServerLevel level, TrialSession session) {
        Map<UUID, Vec3> expected = fixtures.get(session.sessionId());
        if (expected == null) return;
        expected.forEach((id, position) -> {
            if (!(level.getEntity(id) instanceof Shulker shulker)) return;
            configure(shulker, session.sessionId(), false);
            if (shulker.position().distanceToSqr(position) > 0.01) {
                shulker.setPos(position.x, position.y, position.z);
                shulker.setDeltaMovement(Vec3.ZERO);
            }
        });
    }

    public Optional<BlockPos> objectiveAt(BlockPos playerPosition, BlockPos origin, TrialEncounterState state) {
        for (BlockPos local : OBJECTIVE_PLATES) {
            BlockPos world = origin.offset(local);
            if ((world.equals(playerPosition) || world.equals(playerPosition.below()))
                    && !state.completedObjectives().contains(world)) return Optional.of(world);
        }
        return Optional.empty();
    }

    public ActivationResult activatePlate(ServerLevel level, ServerPlayer player, BlockPos plate,
            TrialEncounterState state, RandomSource random) {
        if (!level.getBlockState(plate).is(Blocks.CHERRY_PRESSURE_PLATE)
                || state.completedObjectives().contains(plate)) return new ActivationResult(false, false, state, false);
        ProgressResult progress = resolveObjective(state, plate, () -> awardsPearl(random));
        TrialEncounterState next = progress.state();
        level.setBlock(plate, Blocks.AIR.defaultBlockState(), 3);
        boolean pearl = progress.pearlAwarded();
        if (pearl) {
            ItemStack reward = new ItemStack(Items.ENDER_PEARL);
            if (!player.getInventory().add(reward)) player.drop(reward, false);
        }
        return new ActivationResult(true, pearl, next, progress.complete());
    }

    public static ProgressResult resolveObjective(TrialEncounterState state, BlockPos plate,
            java.util.function.BooleanSupplier pearlRoll) {
        if (state.completedObjectives().contains(plate)) return new ProgressResult(false, false, state, false);
        TrialEncounterState next = advanceObjective(state, plate);
        return new ProgressResult(true, pearlRoll.getAsBoolean(), next,
                next.completedObjectives().size() == OBJECTIVE_PLATES.size());
    }

    public static TrialEncounterState advanceObjective(TrialEncounterState state, BlockPos plate) {
        if (state.completedObjectives().contains(plate)) return state;
        var completed = new ArrayList<>(state.completedObjectives()); completed.add(plate.immutable());
        return state.withObjectives(completed);
    }
    public static boolean awardsPearl(RandomSource random) { return random.nextFloat() < 0.25F; }

    public void cleanup(ServerLevel level, TrialSession session, InstanceBounds bounds) {
        for (UUID id : session.progress().encounter().encounterEntities()) {
            Entity entity = level.getEntity(id); if (entity != null) entity.discard();
        }
        level.getEntitiesOfClass(ShulkerBullet.class, box(bounds).inflate(32.0)).forEach(Entity::discard);
        fixtures.remove(session.sessionId());
    }

    public static boolean encounterShulker(Shulker shulker) { return shulker.getTags().contains(TAG); }
    private static void configure(Shulker shulker, UUID sessionId, boolean intro) {
        shulker.addTag(TAG); shulker.addTag("cosmicpve.trial." + sessionId);
        shulker.setPersistenceRequired(); shulker.setNoAi(intro); shulker.setSilent(false);
        shulker.setInvulnerable(true); shulker.setNoGravity(false); shulker.setCanPickUpLoot(false); shulker.skipDropExperience();
    }
    private static AABB box(InstanceBounds bounds) {
        return new AABB(bounds.min().getX(), bounds.min().getY(), bounds.min().getZ(),
                bounds.max().getX()+1.0, bounds.max().getY()+1.0, bounds.max().getZ()+1.0);
    }
    public record ActivationResult(boolean accepted, boolean pearlAwarded, TrialEncounterState state, boolean complete) {}
    public record ProgressResult(boolean accepted, boolean pearlAwarded, TrialEncounterState state, boolean complete) {}
}
