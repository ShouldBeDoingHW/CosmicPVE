package com.cosmicpve.trial;

import net.minecraft.resources.Identifier;

/** Low-overhead rolling diagnostics for the active Trial service; never logs per tick. */
public final class TrialPerformanceTracker {
    private static final int WINDOW = 200;
    private final long[] samples = new long[WINDOW];
    private int count;
    private int cursor;
    private String state = "inactive";
    private String room = "none";
    private int participants;

    public void record(long nanos, TrialSession session) {
        samples[cursor] = Math.max(0L, nanos);
        cursor = (cursor + 1) % WINDOW;
        count = Math.min(WINDOW, count + 1);
        state = session.state().name();
        room = session.currentRoom().map(Identifier::toString).orElse("none");
        participants = session.participants().size();
    }

    public Snapshot snapshot() {
        long total = 0L, maximum = 0L;
        for (int i = 0; i < count; i++) { total += samples[i]; maximum = Math.max(maximum, samples[i]); }
        return new Snapshot(count, count == 0 ? 0L : total / count, maximum, state, room, participants);
    }

    public record Snapshot(int samples, long averageNanos, long maximumNanos,
                           String state, String room, int participants) {
        public long averageMicros() { return averageNanos / 1_000L; }
        public long maximumMicros() { return maximumNanos / 1_000L; }
    }
}
