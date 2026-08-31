package com.cosmicpve.economy.flashsale;

import java.util.List;

public final class FlashSaleSchedule {
    public static final long MINIMUM_START_INTERVAL = 54_000L;
    public static final long MAXIMUM_START_INTERVAL = 90_000L;
    public static final long DURATION = 6_000L;
    public static final long REMINDER_AGE = 4_800L;

    private FlashSaleSchedule() {}

    public static long nextInterval(IntRandom random) {
        return MINIMUM_START_INTERVAL + random.nextInt((int) (MAXIMUM_START_INTERVAL - MINIMUM_START_INTERVAL + 1));
    }
    public static FlashSaleEntry select(List<FlashSaleEntry> rows, IntRandom random) {
        if (rows.isEmpty()) throw new IllegalArgumentException("No selectable Flash Sale rows");
        return rows.get(random.nextInt(rows.size()));
    }
    public static FlashSalePriceTier selectTier(IntRandom random) {
        return FlashSalePriceTier.values()[random.nextInt(FlashSalePriceTier.values().length)];
    }
    public static boolean reminderDue(FlashSaleActive active, long now) {
        return !active.reminderSent() && now >= active.startTick() + REMINDER_AGE && now < active.endTick();
    }
    public static boolean expired(FlashSaleActive active, long now) { return now >= active.endTick(); }

    @FunctionalInterface public interface IntRandom { int nextInt(int bound); }
}
