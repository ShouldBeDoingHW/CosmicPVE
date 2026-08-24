package com.cosmicpve.trial;

import com.cosmicpve.instance.InstanceBounds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

public record TrialSession(int dataVersion, UUID sessionId, TrialLifecycleState state,
        Identifier portalDimension, BlockPos portalOrigin, List<BlockPos> portalBlocks,
        List<UUID> participants, List<UUID> removedParticipants, int stateTicksRemaining,
        int timerTicks, Optional<Identifier> currentRoom, List<InstanceBounds> protectedBounds,
        boolean initialDecision, TrialProgress progress, long transitionSerial, TrialOwner owner) {
    public static final int DATA_VERSION = 3;
    public static final int INITIAL_TIMER_TICKS = 12_000;
    public static final int JOIN_TICKS = 600;
    public static final int ROOM_INTRO_TICKS = 100;
    public static final int MAX_PARTICIPANTS = 4;

    public static final Codec<TrialSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TrialSession::dataVersion),
            UUIDUtil.CODEC.fieldOf("session_id").forGetter(TrialSession::sessionId),
            TrialLifecycleState.CODEC.fieldOf("state").forGetter(TrialSession::state),
            Identifier.CODEC.fieldOf("portal_dimension").forGetter(TrialSession::portalDimension),
            BlockPos.CODEC.fieldOf("portal_origin").forGetter(TrialSession::portalOrigin),
            BlockPos.CODEC.listOf().fieldOf("portal_blocks").forGetter(TrialSession::portalBlocks),
            UUIDUtil.CODEC.listOf().fieldOf("participants").forGetter(TrialSession::participants),
            UUIDUtil.CODEC.listOf().optionalFieldOf("removed_participants", List.of())
                    .forGetter(TrialSession::removedParticipants),
            Codec.INT.fieldOf("state_ticks_remaining").forGetter(TrialSession::stateTicksRemaining),
            Codec.INT.fieldOf("timer_ticks").forGetter(TrialSession::timerTicks),
            Identifier.CODEC.optionalFieldOf("current_room").forGetter(TrialSession::currentRoom),
            InstanceBounds.CODEC.listOf().fieldOf("protected_bounds").forGetter(TrialSession::protectedBounds),
            Codec.BOOL.optionalFieldOf("initial_decision", true).forGetter(TrialSession::initialDecision),
            TrialProgress.CODEC.optionalFieldOf("progress", TrialProgress.EMPTY).forGetter(TrialSession::progress),
            Codec.LONG.optionalFieldOf("transition_serial", 0L).forGetter(TrialSession::transitionSerial),
            TrialOwner.CODEC.optionalFieldOf("owner", TrialOwner.DEVELOPMENT).forGetter(TrialSession::owner)
    ).apply(instance, TrialSession::new));

    public TrialSession {
        portalBlocks = List.copyOf(portalBlocks);
        participants = List.copyOf(participants);
        removedParticipants = List.copyOf(removedParticipants);
        protectedBounds = List.copyOf(protectedBounds);
        if (timerTicks < 0 || stateTicksRemaining < 0) throw new IllegalArgumentException("Trial timers cannot be negative");
        if (participants.size() > MAX_PARTICIPANTS) throw new IllegalArgumentException("Trial party exceeds four players");
    }

    public static TrialSession joining(UUID id, Identifier dimension, BlockPos origin, List<BlockPos> portalBlocks,
                                       List<InstanceBounds> bounds) {
        return joining(id, dimension, origin, portalBlocks, bounds, TrialOwner.DEVELOPMENT);
    }

    public static TrialSession joining(UUID id, Identifier dimension, BlockPos origin, List<BlockPos> portalBlocks,
                                       List<InstanceBounds> bounds, TrialOwner owner) {
        return new TrialSession(DATA_VERSION, id, TrialLifecycleState.JOINING, dimension, origin, portalBlocks,
                List.of(), List.of(), JOIN_TICKS, INITIAL_TIMER_TICKS, Optional.empty(), bounds, true,
                TrialProgress.EMPTY, 0L, owner);
    }

    public boolean acceptsJoins() { return initialDecision && (state == TrialLifecycleState.JOINING || state == TrialLifecycleState.DECISION); }
    public boolean activeParticipant(UUID id) { return participants.contains(id); }

    public TrialSession addParticipant(UUID id) {
        if (activeParticipant(id)) return this;
        if (!acceptsJoins() || participants.size() >= MAX_PARTICIPANTS) throw new IllegalStateException("Trial cannot accept another participant");
        var next = new ArrayList<>(participants); next.add(id);
        return copy(TrialLifecycleState.DECISION, next, removedParticipants, stateTicksRemaining, timerTicks,
                currentRoom, protectedBounds, true, progress, transitionSerial + 1);
    }

    public TrialSession removeParticipant(UUID id) {
        if (!participants.contains(id)) return this;
        var next = new ArrayList<>(participants); next.remove(id);
        var removed = new ArrayList<>(removedParticipants); if (!removed.contains(id)) removed.add(id);
        return copy(state, next, removed, stateTicksRemaining, timerTicks, currentRoom, protectedBounds,
                initialDecision, progress, transitionSerial + 1);
    }

    public TrialSession withState(TrialLifecycleState nextState, int ticks, Optional<Identifier> room,
                                  boolean initial, List<InstanceBounds> bounds) {
        return copy(nextState, participants, removedParticipants, ticks, timerTicks, room, bounds, initial,
                progress, transitionSerial + 1);
    }

    public TrialSession withStateTicks(int ticks) {
        return copy(state, participants, removedParticipants, ticks, timerTicks, currentRoom, protectedBounds,
                initialDecision, progress, transitionSerial);
    }

    public TrialSession withTimer(int ticks) {
        return copy(state, participants, removedParticipants, stateTicksRemaining, Math.max(0, ticks), currentRoom,
                protectedBounds, initialDecision, progress, transitionSerial);
    }

    public TrialSession withProgress(TrialProgress nextProgress) {
        return copy(state, participants, removedParticipants, stateTicksRemaining, timerTicks, currentRoom,
                protectedBounds, initialDecision, nextProgress, transitionSerial + 1);
    }

    public TrialSession withTimerAndProgress(int ticks, TrialProgress nextProgress) {
        return copy(state, participants, removedParticipants, stateTicksRemaining, Math.max(0, ticks), currentRoom,
                protectedBounds, initialDecision, nextProgress, transitionSerial + 1);
    }

    private TrialSession copy(TrialLifecycleState nextState, List<UUID> nextParticipants, List<UUID> removed,
            int stateTicks, int timer, Optional<Identifier> room, List<InstanceBounds> bounds,
            boolean initial, TrialProgress nextProgress, long serial) {
        return new TrialSession(dataVersion, sessionId, nextState, portalDimension, portalOrigin, portalBlocks,
                nextParticipants, removed, stateTicks, timer, room, bounds, initial, nextProgress, serial, owner);
    }
}
