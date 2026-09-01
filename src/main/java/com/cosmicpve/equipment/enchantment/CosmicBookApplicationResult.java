package com.cosmicpve.equipment.enchantment;

import java.util.OptionalInt;
import net.minecraft.resources.Identifier;

public record CosmicBookApplicationResult(
        Outcome outcome, Identifier enchantmentId, int requestedLevel, int appliedLevel,
        int successRate, OptionalInt successRoll, int destroyRate, OptionalInt destroyRoll,
        int slotsUsed, int capacity, boolean protectedBefore) {
    public enum Outcome {
        REJECTED_INVALID_BOOK, REJECTED_TARGET, REJECTED_LEVEL, REJECTED_CAPACITY,
        REJECTED_EXISTING_LEVEL, REJECTED_HEROIC_PREREQUISITE, REJECTED_HEROIC_COUNTERPART,
        STALE_TARGET, SUCCESS, FAILED_SURVIVED, FAILED_DESTROYED, FAILED_PROTECTED
    }
    public boolean consumedBook() { return switch (outcome) {
        case SUCCESS, FAILED_SURVIVED, FAILED_DESTROYED, FAILED_PROTECTED -> true;
        default -> false;
    }; }
}
