package com.cosmicpve.reward.animation;

public final class LootAnimationTimeline {
    public static final int REVEAL_TICK = 100;
    public static final int CLOSE_TICK = 120;
    public static final int PREVIEW_INTERVAL = 5;
    private LootAnimationTimeline() {}
    public static int countdown(int elapsed) { return elapsed >= REVEAL_TICK ? 0 : Math.max(1, 5 - Math.max(0, elapsed) / 20); }
    public static boolean previewDue(int elapsed) { return elapsed >= 0 && elapsed < REVEAL_TICK && elapsed % PREVIEW_INTERVAL == 0; }
    public static int previewOrdinal(int elapsed) { return Math.max(0, elapsed) / PREVIEW_INTERVAL; }
    public static float previewPitch(int ordinal) { return ordinal % 2 == 0 ? 1.0F : 1.3F; }
}
