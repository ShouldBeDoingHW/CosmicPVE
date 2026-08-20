package com.cosmicpve.combat.stack;

import java.util.Optional;

public record StackTransferResult(
        StackMutationStatus status,
        Optional<CombatStackInstance> transferred,
        int sourceFinalCount,
        int recipientFinalCount) {
    public StackTransferResult {
        transferred = transferred == null ? Optional.empty() : transferred;
    }
}
