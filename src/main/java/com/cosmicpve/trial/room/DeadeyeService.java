package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Runtime state for the authored two-piece Deadeye room. All coordinates are room-local. */
public final class DeadeyeService {
    public static final int EAST_OFFSET_X = 41;
    public static final int SECTION_COUNT = 4;
    public static final BlockPos WEST_SPAWN_MARKER = new BlockPos(2, 16, 21);
    public static final BlockPos FINAL_LEVER_LOCAL = new BlockPos(EAST_OFFSET_X + 27, 23, 21);

    public enum Section { DIAMOND, GOLD, RESIN_PURPUR, CRIMSON }

    private static final List<BlockPos> TARGETS = List.of(
            new BlockPos(11, 20, 3),
            new BlockPos(19, 20, 40),
            new BlockPos(30, 21, 39),
            new BlockPos(EAST_OFFSET_X + 27, 22, 6));

    private static final Map<Section, List<BlockPos>> REVEAL_GROUPS = buildRevealGroups();
    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, BlockPos origin,
            BlockPos resolvedSpawnMarker, InstanceBounds bounds) {
        var sections = new EnumMap<Section, Map<BlockPos, BlockState>>(Section.class);
        for (Section section : Section.values()) {
            var authored = new LinkedHashMap<BlockPos, BlockState>();
            for (BlockPos local : REVEAL_GROUPS.get(section)) {
                BlockPos world = origin.offset(local);
                if (!bounds.contains(world)) throw new IllegalStateException("Deadeye reveal block outside room bounds: " + local);
                BlockState state = level.getBlockState(world);
                if (state.isAir()) throw new IllegalStateException("Deadeye authored reveal block is missing: " + local);
                authored.put(world.immutable(), state);
            }
            sections.put(section, Map.copyOf(authored));
        }
        for (Map<BlockPos, BlockState> section : sections.values()) {
            for (BlockPos pos : section.keySet()) level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
        }
        int fallY = resolvedSpawnMarker.getY() - 10;
        attempts.put(session.sessionId(), new Attempt(origin.immutable(), fallY, Map.copyOf(sections)));
        return new TrialEncounterState(
                java.util.Arrays.stream(Section.values()).map(Enum::name).toList(), 0,
                List.of(), List.of(), List.of(), TARGETS.stream().map(origin::offset).toList(), List.of());
    }

    public RevealResult hitTarget(ServerLevel level, TrialSession session, BlockPos target) {
        Attempt attempt = attempts.get(session.sessionId());
        int progress = session.progress().encounter().sequenceProgress();
        if (attempt == null || progress < 0 || progress >= SECTION_COUNT) return RevealResult.REJECTED;
        if (!expectedTarget(attempt.origin(), progress, target)) return RevealResult.REJECTED;
        Section section = Section.values()[progress];
        attempt.sections().get(section).forEach((pos, state) -> level.setBlock(pos, state, 2));
        int next = progress + 1;
        TrialEncounterState current = session.progress().encounter();
        var claimed = new ArrayList<>(current.claimedTargets()); claimed.add(target.immutable());
        TrialEncounterState state = new TrialEncounterState(current.hiddenSequence(), next,
                current.pillarMaterials(), claimed, current.completedCircuits(),
                current.completedObjectives(), current.encounterEntities());
        return new RevealResult(true, section, state, next == SECTION_COUNT);
    }

    public boolean isFinalLever(UUID sessionId, BlockPos position) {
        Attempt attempt = attempts.get(sessionId);
        return attempt != null && position.equals(attempt.origin().offset(FINAL_LEVER_LOCAL));
    }

    public boolean canComplete(UUID sessionId, BlockPos position, TrialEncounterState encounter) {
        return isFinalLever(sessionId, position) && completionReady(encounter.sequenceProgress());
    }

    public OptionalInt fallThreshold(UUID sessionId) {
        Attempt attempt = attempts.get(sessionId);
        return attempt == null ? OptionalInt.empty() : OptionalInt.of(attempt.fallY());
    }

    public void cleanup(UUID sessionId) { attempts.remove(sessionId); }

    public static List<BlockPos> targets() { return TARGETS; }
    public static Map<Section, List<BlockPos>> revealGroups() { return REVEAL_GROUPS; }
    public static boolean expectedTarget(BlockPos origin, int progress, BlockPos target) {
        return progress >= 0 && progress < TARGETS.size() && origin.offset(TARGETS.get(progress)).equals(target);
    }
    public static boolean completionReady(int progress) { return progress == SECTION_COUNT; }

    private static Map<Section, List<BlockPos>> buildRevealGroups() {
        var groups = new EnumMap<Section, List<BlockPos>>(Section.class);
        groups.put(Section.DIAMOND, positions(
                14,13,9, 14,14,9, 14,15,9, 2,17,15, 5,17,12, 2,18,12, 8,18,12,
                10,18,10, 10,22,10,
                10,19,10, 10,20,10, 10,21,10,
                13,13,9, 13,14,9, 13,15,9));
        groups.put(Section.GOLD, positions(
                16,13,11, 16,13,14, 16,13,17, 17,15,20, 20,16,20, 20,16,24,
                16,14,17, 20,15,28, 22,15,31, 16,15,17));
        var resinPurpur = new ArrayList<BlockPos>(positions(
                29,15,31, 31,16,33, 33,17,30, 33,17,34, 35,17,19, 35,17,23, 35,17,27,
                35,21,19, 35,21,23, 25,15,31, 35,18,19, 35,18,23, 35,19,19, 35,19,23,
                35,20,19, 35,20,23, 36,17,16, 36,18,13, 37,18,10, 40,19,10,
                40,20,2, 40,21,2, 40,22,2));
        resinPurpur.addAll(offsetX(positions(1,19,10, 3,19,13, 0,20,3, 6,20,14,
                0,21,3, 9,21,14, 0,22,3), EAST_OFFSET_X));
        groups.put(Section.RESIN_PURPUR, List.copyOf(resinPurpur));
        groups.put(Section.CRIMSON, offsetX(positions(
                13,22,18, 15,23,20, 17,24,22, 17,24,26, 21,24,27,
                12,21,15, 24,23,26), EAST_OFFSET_X));
        return Map.copyOf(groups);
    }

    private static List<BlockPos> positions(int... coordinates) {
        if (coordinates.length % 3 != 0) throw new IllegalArgumentException("Coordinates must be xyz triples");
        var result = new ArrayList<BlockPos>(coordinates.length / 3);
        for (int index = 0; index < coordinates.length; index += 3)
            result.add(new BlockPos(coordinates[index], coordinates[index + 1], coordinates[index + 2]));
        return List.copyOf(result);
    }

    private static List<BlockPos> offsetX(List<BlockPos> positions, int amount) {
        return positions.stream().map(pos -> pos.offset(amount, 0, 0)).toList();
    }

    private record Attempt(BlockPos origin, int fallY, Map<Section, Map<BlockPos, BlockState>> sections) {}
    public record RevealResult(boolean accepted, Section section, TrialEncounterState state, boolean allRevealed) {
        private static final RevealResult REJECTED = new RevealResult(false, null, TrialEncounterState.EMPTY, false);
    }
}
