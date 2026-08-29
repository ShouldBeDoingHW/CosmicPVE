package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import com.cosmicpve.trial.TrialTitleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Bounded runtime for Haze and Seek. Structure blocks remain the source of marker positions. */
public final class HazeAndSeekService {
    public static final int BLIND_TICKS = 100;
    public static final int CLEAR_TICKS = 200;
    public static final int BLINDNESS_AMPLIFIER = 1;
    private static final int OWNED_LEASE_TICKS = 30;
    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, InstanceBounds bounds, RandomSource random) {
        List<BlockPos> candidates = markers(level, bounds, Blocks.GOLD_BLOCK);
        List<BlockPos> exits = markers(level, bounds, Blocks.DIAMOND_BLOCK);
        if (candidates.size() != 9 || exits.size() != 1)
            throw new IllegalStateException("Haze and Seek requires nine Gold markers and one Diamond marker");
        int required = requiredPlateCount(session.participants().size());
        shuffle(candidates, random);
        var selected = new LinkedHashSet<>(candidates.subList(0, required));
        selected.forEach(pos -> level.setBlock(pos.above(), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 3));
        Attempt attempt = new Attempt(session.sessionId(), bounds, Set.copyOf(selected), new LinkedHashSet<>(),
                exits.getFirst(), false, true, 0, new LinkedHashSet<>());
        attempts.put(session.sessionId(), attempt);
        return encounter(attempt);
    }

    public void activate(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return;
        attempt.blindPhase = true; attempt.phaseTicks = 0;
        reconcileBlindness(level, session, attempt, true);
        playForParty(level, session, blindStartSound());
    }

    public TickResult tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return new TickResult(session.progress().encounter(), false);
        attempt.phaseTicks++;
        int duration = attempt.blindPhase ? BLIND_TICKS : CLEAR_TICKS;
        if (attempt.phaseTicks >= duration) {
            attempt.phaseTicks = 0; attempt.blindPhase = !attempt.blindPhase;
            if (attempt.blindPhase) {
                reconcileBlindness(level, session, attempt, true);
                playForParty(level, session, blindStartSound());
            } else {
                playForParty(level, session, blindClearSound());
                reconcileBlindness(level, session, attempt, false);
            }
        } else if (attempt.blindPhase && attempt.phaseTicks % 10 == 0) {
            reconcileBlindness(level, session, attempt, true);
        }
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player == null) continue;
            for (BlockPos marker : attempt.selected) {
                if (!attempt.completed.contains(marker) && standingOn(player, marker.above())) {
                    attempt.completed.add(marker);
                    level.setBlock(marker.above(), Blocks.AIR.defaultBlockState(), 3);
                    int remaining = attempt.selected.size() - attempt.completed.size();
                    notifyPlate(level, session, remaining);
                    if (remaining == 0) createPortal(level, attempt);
                }
            }
            if (shouldCompleteRoom(attempt.portalCreated, insidePortal(player, attempt.exitMarker)))
                return new TickResult(encounter(attempt), true);
        }
        return new TickResult(encounter(attempt), false);
    }

    private static void reconcileBlindness(ServerLevel level, TrialSession session, Attempt attempt, boolean apply) {
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player == null) continue;
            var current = player.getEffect(MobEffects.BLINDNESS);
            boolean ours = attempt.ownedBlindness.contains(id);
            if (apply) {
                if (ours && !managed(current)) { attempt.ownedBlindness.remove(id); continue; }
                if (!ours && current != null) continue;
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, OWNED_LEASE_TICKS,
                        BLINDNESS_AMPLIFIER, true, false, false));
                attempt.ownedBlindness.add(id);
            } else if (ours) {
                if (managed(current)) player.removeEffect(MobEffects.BLINDNESS);
                attempt.ownedBlindness.remove(id);
            }
        }
    }

    private static boolean managed(MobEffectInstance effect) {
        return effect != null && effect.getAmplifier() == BLINDNESS_AMPLIFIER && effect.isAmbient()
                && !effect.isVisible() && !effect.showIcon() && effect.getDuration() <= OWNED_LEASE_TICKS;
    }

    private static boolean standingOn(ServerPlayer player, BlockPos plate) {
        return player.getBoundingBox().intersects(new AABB(plate).inflate(0.05D, 0.25D, 0.05D));
    }

    private static boolean insidePortal(ServerPlayer player, BlockPos marker) {
        return player.getBoundingBox().intersects(new AABB(marker.above()).expandTowards(0, 2, 0));
    }

    private static void createPortal(ServerLevel level, Attempt attempt) {
        if (attempt.portalCreated) return;
        level.setBlock(attempt.exitMarker.above(), com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
        level.setBlock(attempt.exitMarker.above(2), com.cosmicpve.registry.ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
        attempt.portalCreated = true;
    }

    public static boolean objectivesComplete(int selected, int completed) {
        return selected > 0 && completed == selected;
    }

    public static boolean shouldCompleteRoom(boolean portalCreated, boolean participantInsidePortal) {
        return portalCreated && participantInsidePortal;
    }

    public static Component plateMessage(int remaining) {
        String text = remaining == 0 ? "You found a pressure plate! Locate the exit portal!"
                : "You found a pressure plate! " + remaining + " more to go!";
        return Component.literal(text).withStyle(style -> style.withColor(0x1B4F2C).withBold(true).withItalic(true));
    }

    public static SoundEvent plateSound() { return SoundEvents.WITHER_SPAWN; }
    public static SoundEvent blindStartSound() { return SoundEvents.BEACON_ACTIVATE; }
    public static SoundEvent blindClearSound() { return SoundEvents.BEACON_DEACTIVATE; }

    private static void notifyPlate(ServerLevel level, TrialSession session, int remaining) {
        for (UUID id : session.participants()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
            if (member == null) continue;
            TrialTitleService.playForPlayer(member, plateSound(), 2.0F, 1.0F);
            member.sendSystemMessage(plateMessage(remaining));
        }
    }

    private static void playForParty(ServerLevel level, TrialSession session, SoundEvent sound) {
        for (UUID id : session.participants()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
            if (member != null) TrialTitleService.playForPlayer(member, sound);
        }
    }

    public void cleanup(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.remove(session.sessionId());
        if (attempt == null) return;
        for (UUID id : List.copyOf(attempt.ownedBlindness)) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && managed(player.getEffect(MobEffects.BLINDNESS))) player.removeEffect(MobEffects.BLINDNESS);
            attempt.ownedBlindness.remove(id);
        }
        attempt.selected.forEach(pos -> level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3));
        if (attempt.portalCreated) {
            level.setBlock(attempt.exitMarker.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(attempt.exitMarker.above(2), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public void removeParticipant(ServerPlayer player, UUID sessionId) {
        Attempt attempt = attempts.get(sessionId);
        if (attempt == null || !attempt.ownedBlindness.remove(player.getUUID())) return;
        if (managed(player.getEffect(MobEffects.BLINDNESS))) player.removeEffect(MobEffects.BLINDNESS);
    }

    public String status(UUID sessionId) {
        Attempt a = attempts.get(sessionId);
        return a == null ? "Haze state unavailable" : "haze plates=" + a.completed.size() + "/" + a.selected.size()
                + " phase=" + (a.blindPhase ? "blind" : "clear") + " phaseTicks=" + a.phaseTicks
                + " portal=" + a.portalCreated;
    }

    public static int requiredPlateCount(int partySize) {
        if (partySize < 1 || partySize > 4) throw new IllegalArgumentException("party size must be 1-4");
        return partySize + 2;
    }

    private static List<BlockPos> markers(ServerLevel level, InstanceBounds bounds, net.minecraft.world.level.block.Block block) {
        var result = new ArrayList<BlockPos>();
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max()))
            if (level.getBlockState(pos).is(block)) result.add(pos.immutable());
        result.sort(BlockPos::compareTo); return result;
    }

    private static <T> void shuffle(List<T> values, RandomSource random) {
        for (int i = values.size() - 1; i > 0; i--) java.util.Collections.swap(values, i, random.nextInt(i + 1));
    }

    private static TrialEncounterState encounter(Attempt attempt) {
        return new TrialEncounterState(List.of(attempt.blindPhase ? "blind" : "clear"), attempt.completed.size(),
                List.of(), List.of(), List.of(), List.copyOf(attempt.completed), List.of());
    }

    public record TickResult(TrialEncounterState encounter, boolean complete) {}
    private static final class Attempt {
        final UUID sessionId; final InstanceBounds bounds; final Set<BlockPos> selected; final Set<BlockPos> completed;
        final BlockPos exitMarker; boolean portalCreated; boolean blindPhase; int phaseTicks;
        final Set<UUID> ownedBlindness;
        Attempt(UUID sessionId, InstanceBounds bounds, Set<BlockPos> selected, Set<BlockPos> completed,
                BlockPos exitMarker, boolean portalCreated, boolean blindPhase, int phaseTicks, Set<UUID> ownedBlindness) {
            this.sessionId=sessionId; this.bounds=bounds; this.selected=selected; this.completed=completed;
            this.exitMarker=exitMarker; this.portalCreated=portalCreated; this.blindPhase=blindPhase;
            this.phaseTicks=phaseTicks; this.ownedBlindness=ownedBlindness;
        }
    }
}
