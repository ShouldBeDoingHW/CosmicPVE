package com.cosmicpve.trial;

/** Canonical abstract progression. Physical encounters never determine the run tier. */
public final class TrialProgression {
    public static final int PHASE_ENTRY_TICKS = 120 * 20;
    private TrialProgression() {}

    public static TrialPhase phaseForRoomOrdinal(int ordinal) {
        if (ordinal < 1) throw new IllegalArgumentException("Room ordinal must be positive");
        if (ordinal >= 13) return TrialPhase.DEMONIC;
        if (ordinal >= 9) return TrialPhase.IMPOSSIBLE;
        if (ordinal >= 5) return TrialPhase.HARDCORE;
        return TrialPhase.APPRENTICE;
    }

    public static TrialPhase phaseAfter(int completed) {
        if (completed < 0) throw new IllegalArgumentException("Completed count cannot be negative");
        return phaseForRoomOrdinal(Math.addExact(completed, 1));
    }

    public static int crossedPhaseBoundaries(int before, int after) {
        validateRange(before, after);
        int count = 0;
        for (int boundary : new int[]{4, 8, 12}) if (before < boundary && after >= boundary) count++;
        return count;
    }

    public static int crossedMadnessThresholds(int before, int after) {
        validateRange(before, after);
        return after / 5 - before / 5;
    }

    public static int phaseEntryTicks(int before, int after) {
        return crossedPhaseBoundaries(before, after) * PHASE_ENTRY_TICKS;
    }

    public static int playedRoomBonusTicks(TrialPhase phase) {
        return switch (phase) { case APPRENTICE -> 600; case HARDCORE -> 300; default -> 0; };
    }

    private static void validateRange(int before, int after) {
        if (before < 0 || after < before) throw new IllegalArgumentException("Invalid progression range");
    }
}
