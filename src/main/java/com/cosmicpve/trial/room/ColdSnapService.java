package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Event-driven state and world mutations for the Cold Snap shortcut. */
public final class ColdSnapService {
    public static final BlockPos SPAWN_MARKER_LOCAL = new BlockPos(5, 10, 17);
    public static final BlockPos GOLD_PLATE_LOCAL = new BlockPos(11, 14, 9);
    public static final BlockPos IRON_PLATE_LOCAL = new BlockPos(1, 14, 32);
    public static final BlockPos DOOR_LOWER_LOCAL = new BlockPos(3, 14, 32);
    public static final BlockPos DOOR_UPPER_LOCAL = new BlockPos(3, 15, 32);
    public static final BlockPos FINAL_LEVER_LOCAL = new BlockPos(36, 15, 2);
    public static final int SECRET_ICE_COUNT = 26;
    private static final String DOOR_UNLOCKED = "cold_snap:door_unlocked";
    private static final String SECRET_REVEALED = "cold_snap:secret_revealed";

    private final Map<UUID, Map<BlockPos, BlockState>> hiddenRoutes = new HashMap<>();

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, BlockPos origin, InstanceBounds bounds) {
        Map<BlockPos, BlockState> route = resolveSecretRoute(level, origin, bounds);
        if (route.size() != SECRET_ICE_COUNT)
            throw new IllegalStateException("Cold Snap secret route resolved " + route.size() + " Ice blocks; expected " + SECRET_ICE_COUNT);
        route.keySet().forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
        hiddenRoutes.put(session.sessionId(), Map.copyOf(route));
        return TrialEncounterState.EMPTY;
    }

    public ActivationResult activateGold(ServerLevel level, TrialSession session, BlockPos origin) {
        TrialEncounterState state = session.progress().encounter();
        if (doorUnlocked(state)) return ActivationResult.ignored(state);
        level.setBlock(origin.offset(DOOR_LOWER_LOCAL), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(origin.offset(DOOR_UPPER_LOCAL), Blocks.AIR.defaultBlockState(), 3);
        return new ActivationResult(true, markDoorUnlocked(state));
    }

    public ActivationResult activateIron(ServerLevel level, TrialSession session) {
        TrialEncounterState state = session.progress().encounter();
        if (!doorUnlocked(state) || secretRevealed(state)) return ActivationResult.ignored(state);
        Map<BlockPos, BlockState> route = hiddenRoutes.get(session.sessionId());
        if (route == null || route.size() != SECRET_ICE_COUNT) return ActivationResult.ignored(state);
        route.forEach((pos, block) -> level.setBlock(pos, block, 3));
        return new ActivationResult(true, markSecretRevealed(state));
    }

    public boolean isFinalLever(BlockPos worldPos, BlockPos origin) {
        return worldPos.equals(origin.offset(FINAL_LEVER_LOCAL));
    }

    public void cleanup(UUID sessionId) { hiddenRoutes.remove(sessionId); }
    public static boolean doorUnlocked(TrialEncounterState state) { return state.completedCircuits().contains(DOOR_UNLOCKED); }
    public static boolean secretRevealed(TrialEncounterState state) { return state.completedCircuits().contains(SECRET_REVEALED); }
    public static TrialEncounterState markDoorUnlocked(TrialEncounterState state) { return addFlag(state, DOOR_UNLOCKED); }
    public static TrialEncounterState markSecretRevealed(TrialEncounterState state) {
        return doorUnlocked(state) ? addFlag(state, SECRET_REVEALED) : state;
    }

    public static boolean isSecretRouteLocal(BlockPos local) {
        boolean first = local.getY() == 13 && local.getX() >= 18 && local.getX() <= 19
                && ((local.getZ() >= 5 && local.getZ() <= 8) || (local.getZ() >= 12 && local.getZ() <= 15));
        boolean second = local.getY() == 14 && local.getX() >= 21 && local.getX() <= 22
                && local.getZ() >= 2 && local.getZ() <= 3;
        boolean third = local.getY() == 14 && local.getX() >= 25 && local.getX() <= 27
                && local.getZ() >= 2 && local.getZ() <= 3;
        return first || second || third;
    }

    private static Map<BlockPos, BlockState> resolveSecretRoute(ServerLevel level, BlockPos origin, InstanceBounds bounds) {
        Map<BlockPos, BlockState> found = new LinkedHashMap<>();
        for (int x = 18; x <= 27; x++) for (int y = 13; y <= 14; y++) for (int z = 2; z <= 15; z++) {
            BlockPos local = new BlockPos(x, y, z);
            if (!isSecretRouteLocal(local)) continue;
            BlockPos world = origin.offset(local);
            if (bounds.contains(world) && level.getBlockState(world).is(Blocks.ICE)) found.put(world.immutable(), level.getBlockState(world));
        }
        return found;
    }

    private static TrialEncounterState addFlag(TrialEncounterState state, String flag) {
        List<String> flags = new ArrayList<>(state.completedCircuits());
        if (!flags.contains(flag)) flags.add(flag);
        return new TrialEncounterState(state.hiddenSequence(), state.sequenceProgress(), state.pillarMaterials(),
                state.claimedTargets(), flags, state.completedObjectives(), state.encounterEntities());
    }

    public record ActivationResult(boolean accepted, TrialEncounterState state) {
        static ActivationResult ignored(TrialEncounterState state) { return new ActivationResult(false, state); }
    }
}
