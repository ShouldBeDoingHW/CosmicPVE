package com.cosmicpve.reward.animation;

public final class LootAnimationTimeline {
    public static final int REVEAL_TICK = 100;
    public static final int FINAL_HOLD_TICKS = 60;
    public static final int CLOSE_TICK = REVEAL_TICK + FINAL_HOLD_TICKS;
    public static final int PREVIEW_INTERVAL = 5;
    private LootAnimationTimeline() {}
    public static int countdown(int elapsed) { return elapsed >= REVEAL_TICK ? 0 : Math.max(1, 5 - Math.max(0, elapsed) / 20); }
    public static boolean previewDue(int elapsed) { return elapsed >= 0 && elapsed < REVEAL_TICK && elapsed % PREVIEW_INTERVAL == 0; }
    public static int previewOrdinal(int elapsed) { return Math.max(0, elapsed) / PREVIEW_INTERVAL; }
    public static boolean previewSoundDue(int elapsed) {
        int within = Math.floorMod(elapsed, PREVIEW_INTERVAL);
        return elapsed >= 0 && elapsed < REVEAL_TICK && (within == 0 || within == 2);
    }
    public static int previewSoundOrdinal(int elapsed) {
        int window = Math.max(0, elapsed) / PREVIEW_INTERVAL;
        return window * 2 + (Math.floorMod(elapsed, PREVIEW_INTERVAL) >= 2 ? 1 : 0);
    }
    private static final float[] PREVIEW_PITCHES = {1.0F, 0.8F, 0.5F, 0.9F, 1.1F, 1.3F, 1.5F, 1.2F};
    public static float previewPitch(int soundOrdinal) {
        return PREVIEW_PITCHES[Math.floorMod(soundOrdinal, PREVIEW_PITCHES.length)];
    }
}
