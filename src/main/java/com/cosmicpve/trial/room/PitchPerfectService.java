package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialSession;
import com.cosmicpve.trial.TrialTitleService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** The authored six-portal Harp puzzle; only its per-attempt state is kept in memory. */
public final class PitchPerfectService {
    public static final float NOTE_VOLUME = 4.0F;
    public static final BlockPos SPAWN_MARKER_LOCAL = new BlockPos(8, 1, 10);
    public static final List<BlockPos> TARGET_PLATES = List.of(
            new BlockPos(8, 2, 9), new BlockPos(7, 2, 10),
            new BlockPos(9, 2, 10), new BlockPos(8, 2, 11));
    public static final List<List<BlockPos>> PORTAL_BUTTONS = List.of(
            List.of(new BlockPos(2, 3, 1), new BlockPos(2, 3, 5)),
            List.of(new BlockPos(2, 3, 8), new BlockPos(2, 3, 12)),
            List.of(new BlockPos(2, 3, 15), new BlockPos(2, 3, 19)),
            List.of(new BlockPos(14, 3, 1), new BlockPos(14, 3, 5)),
            List.of(new BlockPos(14, 3, 8), new BlockPos(14, 3, 12)),
            List.of(new BlockPos(14, 3, 15), new BlockPos(14, 3, 19)));
    private static final int[] PORTAL_CENTERS_Z = {3, 10, 17};
    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public static int requiredRounds(int participants) {
        if (participants < 1 || participants > 4) throw new IllegalArgumentException("Pitch Perfect party size must be 1–4");
        return participants + 2;
    }

    public static Round newRound(RandomSource random, int generation) {
        int[] pool = new int[25];
        for (int i = 0; i < pool.length; i++) pool[i] = i;
        for (int i = 0; i < 6; i++) {
            int selected = i + random.nextInt(pool.length - i);
            int swap = pool[i]; pool[i] = pool[selected]; pool[selected] = swap;
        }
        List<Integer> notes = Arrays.stream(pool, 0, 6).boxed().toList();
        return new Round(notes, notes.get(random.nextInt(6)), generation);
    }

    public static float pitch(int note) {
        if (note < 0 || note > 24) throw new IllegalArgumentException("Harp note must be 0–24");
        return (float) Math.pow(2.0, (note - 12) / 12.0);
    }

    public static Component successMessage(int completed, int required) {
        return Component.literal("Pitch number " + completed + " identified! Only " + (required - completed) + " more to go!")
                .withStyle(style -> style.withBold(true).withItalic(true).withColor(0xE1BAE8));
    }

    public void initialize(ServerLevel level, TrialSession session, BlockPos origin, InstanceBounds bounds) {
        requireBlock(level, bounds, origin.offset(SPAWN_MARKER_LOCAL), Blocks.BLACKSTONE);
        for (BlockPos local : TARGET_PLATES)
            requireBlock(level, bounds, origin.offset(local), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        for (int portal = 0; portal < 6; portal++) {
            for (BlockPos button : PORTAL_BUTTONS.get(portal))
                requireBlock(level, bounds, origin.offset(button), Blocks.BAMBOO_BUTTON);
            BlockPos water = origin.offset(new BlockPos(portal < 3 ? 1 : 15, 2, PORTAL_CENTERS_Z[portal % 3]));
            requireBlock(level, bounds, water, Blocks.WATER);
        }
        attempts.put(session.sessionId(), new Attempt(requiredRounds(session.participants().size()),
                newRound(RandomSource.create(), 1), origin.offset(SPAWN_MARKER_LOCAL).above()));
    }

    private static void requireBlock(ServerLevel level, InstanceBounds bounds, BlockPos pos,
                                     net.minecraft.world.level.block.Block expected) {
        if (!bounds.contains(pos) || !level.getBlockState(pos).is(expected))
            throw new IllegalStateException("Pitch Perfect authored structure expected " + expected + " at " + pos);
    }

    public Attempt attempt(UUID sessionId) { return attempts.get(sessionId); }

    /** Ordinary player-position polling is bounded by party size, not by structure volume. */
    public boolean tick(ServerLevel level, TrialSession session, BlockPos origin) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || attempt.complete) return false;
        long tick = level.getServer().getTickCount();
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player == null || player.level() != level || !player.isAlive()) continue;
            BlockPos feet = player.blockPosition();
            int portal = portalAt(feet.subtract(origin));
            if (portal < 0 || !level.getBlockState(feet).is(Blocks.WATER)) continue;
            int generation = attempt.round.generation();
            GuessResult result = guess(attempt, portal, generation, tick, RandomSource.create());
            if (!result.accepted()) return false;
            if (result.correct()) {
                Component message = successMessage(attempt.completed, attempt.required);
                for (UUID participant : session.participants()) {
                    ServerPlayer member = level.getServer().getPlayerList().getPlayer(participant);
                    if (member != null && member.level() == level) member.sendSystemMessage(message);
                }
            }
            if (!result.complete()) teleportParty(level, session, attempt.spawn);
            return result.complete();
        }
        return false;
    }

    private static void teleportParty(ServerLevel level, TrialSession session, BlockPos spawn) {
        for (UUID id : session.participants()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
            if (member != null && member.level() == level && member.isAlive())
                member.teleportTo(level, spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5,
                        Set.<Relative>of(), 0.0F, 0.0F, false);
        }
    }

    public void replayButton(ServerLevel level, TrialSession session, BlockPos origin,
                             ServerPlayer player, BlockPos worldPos) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || attempt.complete || !session.activeParticipant(player.getUUID())
                || player.level() != level || !level.getBlockState(worldPos).is(Blocks.BAMBOO_BUTTON)) return;
        int portal = portalForButton(worldPos.subtract(origin));
        if (portal >= 0) play(level, session, attempt.round.portalNotes().get(portal));
    }

    public void replayTargetPlate(ServerLevel level, TrialSession session, BlockPos origin,
                                  BlockPos worldPos, BlockState state) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || attempt.complete || !TARGET_PLATES.contains(worldPos.subtract(origin))
                || !state.is(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)
                || !state.hasProperty(BlockStateProperties.POWER)) return;
        if (state.getValue(BlockStateProperties.POWER) == 0) {
            attempt.poweredPlates.remove(worldPos);
            return;
        }
        boolean participantPresent = level.getEntitiesOfClass(ServerPlayer.class,
                new net.minecraft.world.phys.AABB(worldPos).inflate(.1),
                player -> session.activeParticipant(player.getUUID()) && player.isAlive()).stream().findAny().isPresent();
        if (participantPresent && attempt.poweredPlates.add(worldPos))
            play(level, session, attempt.round.targetNote());
    }

    private static void play(ServerLevel level, TrialSession session, int note) {
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.level() == level && player.isAlive())
                TrialTitleService.playForPlayer(player, SoundEvents.NOTE_BLOCK_HARP.value(), NOTE_VOLUME, pitch(note));
        }
    }

    public static int portalForButton(BlockPos local) {
        for (int i = 0; i < PORTAL_BUTTONS.size(); i++)
            if (PORTAL_BUTTONS.get(i).contains(local)) return i;
        return -1;
    }

    public static int portalAt(BlockPos local) {
        if (local.getY() < 1 || local.getY() > 5) return -1;
        int side = local.getX() >= 1 && local.getX() <= 2 ? 0
                : local.getX() >= 14 && local.getX() <= 15 ? 3 : -1;
        if (side < 0) return -1;
        for (int i = 0; i < PORTAL_CENTERS_Z.length; i++)
            if (Math.abs(local.getZ() - PORTAL_CENTERS_Z[i]) <= 1) return side + i;
        return -1;
    }

    public static GuessResult guess(Attempt attempt, int portal, int generation, long tick, RandomSource random) {
        if (portal < 0 || portal >= 6 || attempt.complete || attempt.resolving
                || generation != attempt.round.generation() || tick <= attempt.lastResolutionTick)
            return GuessResult.IGNORED;
        attempt.resolving = true;
        try {
            boolean correct = attempt.round.portalNotes().get(portal) == attempt.round.targetNote();
            if (correct) attempt.completed++;
            attempt.lastResolutionTick = tick;
            attempt.complete = attempt.completed == attempt.required;
            if (!attempt.complete) attempt.round = newRound(random, generation + 1);
            return new GuessResult(true, correct, attempt.complete);
        } finally {
            attempt.resolving = false;
        }
    }

    public void cleanup(UUID sessionId) { attempts.remove(sessionId); }

    public record Round(List<Integer> portalNotes, int targetNote, int generation) {
        public Round {
            portalNotes = List.copyOf(portalNotes);
            if (portalNotes.size() != 6 || new HashSet<>(portalNotes).size() != 6
                    || portalNotes.stream().anyMatch(note -> note < 0 || note > 24)
                    || !portalNotes.contains(targetNote) || generation < 1)
                throw new IllegalArgumentException("Invalid Pitch Perfect round");
        }
    }

    public static final class Attempt {
        private final int required;
        private final BlockPos spawn;
        private final Set<BlockPos> poweredPlates = new HashSet<>();
        private Round round;
        private int completed;
        private long lastResolutionTick = -1;
        private boolean resolving;
        private boolean complete;

        public Attempt(int required, Round round, BlockPos spawn) {
            if (required < 3 || required > 6) throw new IllegalArgumentException("Invalid round target");
            this.required = required; this.round = round; this.spawn = spawn;
        }
        public int required() { return required; }
        public int completed() { return completed; }
        public Round round() { return round; }
        public BlockPos spawn() { return spawn; }
    }

    public record GuessResult(boolean accepted, boolean correct, boolean complete) {
        public static final GuessResult IGNORED = new GuessResult(false, false, false);
    }
}
