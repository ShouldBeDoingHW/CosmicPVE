package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TrialPortalModifiers(int dataVersion, int timeBonusSeconds, int skipRooms, int insuranceItems,
        int famePercent, int madnessOptionBonus) {
    public static final int DATA_VERSION = 3;
    public static final int MAX_SKIP_ROOMS = 1000;
    public static final int MAX_TIME_SECONDS = 86400;
    public static final int MAX_INSURANCE_ITEMS = 1000;
    public static final int MAX_FAME_PERCENT = 100000;
    public static final int MAX_MADNESS_BONUS = 1000;
    public static final TrialPortalModifiers EMPTY = new TrialPortalModifiers(DATA_VERSION, 0, 0, 0, 0, 0);
    private static final Codec<TrialPortalModifiers> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TrialPortalModifiers::dataVersion),
            Codec.INT.optionalFieldOf("time_bonus_seconds").forGetter(v -> java.util.Optional.of(v.timeBonusSeconds())),
            Codec.INT.optionalFieldOf("skip_rooms", 0).forGetter(TrialPortalModifiers::skipRooms),
            Codec.INT.optionalFieldOf("insurance_items").forGetter(v -> java.util.Optional.of(v.insuranceItems())),
            Codec.INT.optionalFieldOf("fame_percent", 0).forGetter(TrialPortalModifiers::famePercent),
            Codec.INT.optionalFieldOf("madness_option_bonus", 0).forGetter(TrialPortalModifiers::madnessOptionBonus),
            Codec.INT.optionalFieldOf("time_minutes", 0).forGetter(v -> 0),
            Codec.INT.optionalFieldOf("insurance_level", 0).forGetter(v -> 0)
    ).apply(instance, (version, seconds, skip, insurance, fame, madness, minutes, legacyInsurance) ->
            new TrialPortalModifiers(version, seconds.orElseGet(() -> legacySeconds(minutes)), skip,
                    insurance.orElse(legacyInsurance), fame, madness)));
    public static final Codec<TrialPortalModifiers> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "Invalid Trial Portal modifiers"));

    public boolean valid() {
        return dataVersion >= 1 && dataVersion <= DATA_VERSION
                && bounded(timeBonusSeconds, MAX_TIME_SECONDS) && bounded(skipRooms, MAX_SKIP_ROOMS)
                && bounded(insuranceItems, MAX_INSURANCE_ITEMS) && bounded(famePercent, MAX_FAME_PERCENT)
                && bounded(madnessOptionBonus, MAX_MADNESS_BONUS);
    }

    private static boolean bounded(int value, int max) { return value >= 0 && value <= max; }
    private static int legacySeconds(int minutes) {
        return minutes >= 0 && minutes <= MAX_TIME_SECONDS / 60 ? minutes * 60 : -1;
    }
    /** Compatibility constructor: historical callers supply minutes. */
    public TrialPortalModifiers(int version, int minutes, int skip, int insurance, int fame) {
        this(version, legacySeconds(minutes), skip, insurance, fame, 0);
    }
    public int timeMinutes() { return timeBonusSeconds / 60; }
    public int insuranceLevel() { return insuranceItems; }
    public int madnessChoices(int remaining) { return Math.min(Math.max(0, remaining), Math.min(5, 2 + madnessOptionBonus)); }

    public TrialPortalModifiers(int dataVersion, int timeMinutes, int skipRooms, int insuranceLevel) {
        this(dataVersion, timeMinutes, skipRooms, insuranceLevel, 0);
    }

    public int value(TrialTrinketType type) {
        return switch (type) { case TIME -> timeMinutes(); case SKIP -> skipRooms; case INSURANCE -> insuranceItems;
            case FAME -> famePercent; case MADNESS -> madnessOptionBonus; };
    }

    public TrialPortalModifiers with(TrialTrinketData trinket) {
        if (trinket == null || !trinket.valid()) throw new IllegalArgumentException("Invalid Trial Trinket");
        int concrete = trinket.type() == TrialTrinketType.TIME ? trinket.value() * 60 : trinket.value();
        int current = trinket.type() == TrialTrinketType.TIME ? timeBonusSeconds : value(trinket.type());
        if (!valid() || !trinket.valid() || concrete <= current)
            throw new IllegalArgumentException("Trial Trinket must be a stronger valid modifier");
        return new TrialPortalModifiers(DATA_VERSION,
                trinket.type() == TrialTrinketType.TIME ? concrete : timeBonusSeconds,
                trinket.type() == TrialTrinketType.SKIP ? concrete : skipRooms,
                trinket.type() == TrialTrinketType.INSURANCE ? concrete : insuranceItems,
                trinket.type() == TrialTrinketType.FAME ? concrete : famePercent,
                trinket.type() == TrialTrinketType.MADNESS ? concrete : madnessOptionBonus);
    }

    public int initialTimerTicks() { return 12_000 + timeBonusSeconds * 20; }
    public boolean isEmpty() { return timeBonusSeconds == 0 && skipRooms == 0 && insuranceItems == 0
            && famePercent == 0 && madnessOptionBonus == 0; }

    public long cashoutFame(long base) {
        if (base < 0) throw new IllegalArgumentException("base Fame cannot be negative");
        if (!valid()) throw new IllegalStateException("Invalid Portal modifier");
        return java.math.BigInteger.valueOf(base).multiply(java.math.BigInteger.valueOf(100L + famePercent))
                .divide(java.math.BigInteger.valueOf(100)).longValueExact();
    }
}
