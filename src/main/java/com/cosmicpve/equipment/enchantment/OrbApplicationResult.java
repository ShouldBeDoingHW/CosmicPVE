package com.cosmicpve.equipment.enchantment;

import java.util.OptionalInt;

public record OrbApplicationResult(
        Outcome outcome, OrbType type, int successRate, OptionalInt successRoll,
        int destroyRate, OptionalInt destroyRoll, int capacityBefore, int capacityAfter,
        boolean protectedBefore) {
    public enum Outcome {
        REJECTED_INVALID_ORB, REJECTED_TARGET, REJECTED_MAX_CAPACITY, STALE_TARGET,
        SUCCESS, FAILED_SURVIVED, FAILED_PROTECTED, FAILED_DESTROYED
    }
    public boolean consumedOrb() {
        return switch (outcome) {
            case SUCCESS, FAILED_SURVIVED, FAILED_PROTECTED, FAILED_DESTROYED -> true;
            default -> false;
        };
    }
}
