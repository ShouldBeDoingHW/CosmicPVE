package com.cosmicpve.combat.execution;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/** Terminal-event receipt. It intentionally has no ordinary or true-damage amount. */
public record ExecutionResult(
        ExecutionCause cause,
        UUID targetId,
        Optional<UUID> attributedPlayerId,
        long sequenceId,
        OptionalLong parentSequenceId,
        boolean executed) {}
