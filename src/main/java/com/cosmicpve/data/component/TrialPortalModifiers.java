package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TrialPortalModifiers(int dataVersion, int timeMinutes, int skipRooms, int insuranceLevel, int famePercent) {
    public static final int DATA_VERSION = 2;
    public static final TrialPortalModifiers EMPTY = new TrialPortalModifiers(DATA_VERSION, 0, 0, 0, 0);
    private static final Codec<TrialPortalModifiers> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TrialPortalModifiers::dataVersion),
            Codec.INT.optionalFieldOf("time_minutes", 0).forGetter(TrialPortalModifiers::timeMinutes),
            Codec.INT.optionalFieldOf("skip_rooms", 0).forGetter(TrialPortalModifiers::skipRooms),
            Codec.INT.optionalFieldOf("insurance_level", 0).forGetter(TrialPortalModifiers::insuranceLevel),
            Codec.INT.optionalFieldOf("fame_percent", 0).forGetter(TrialPortalModifiers::famePercent)
    ).apply(instance, TrialPortalModifiers::new));
    public static final Codec<TrialPortalModifiers> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "Invalid Trial Portal modifiers"));

    public boolean valid() {
        return (dataVersion == 1 || dataVersion == DATA_VERSION)
                && (timeMinutes == 0 || timeMinutes == 1 || timeMinutes == 3 || timeMinutes == 5)
                && skipRooms >= 0 && skipRooms <= 3 && insuranceLevel >= 0 && insuranceLevel <= 3
                && (famePercent == 0 || famePercent == 33 || famePercent == 66 || famePercent == 100);
    }

    public TrialPortalModifiers(int dataVersion, int timeMinutes, int skipRooms, int insuranceLevel) {
        this(dataVersion, timeMinutes, skipRooms, insuranceLevel, 0);
    }

    public int value(TrialTrinketType type) {
        return switch (type) { case TIME -> timeMinutes; case SKIP -> skipRooms; case INSURANCE -> insuranceLevel; case FAME -> famePercent; };
    }

    public TrialPortalModifiers with(TrialTrinketData trinket) {
        if (!trinket.valid() || trinket.value() <= value(trinket.type()))
            throw new IllegalArgumentException("Trial Trinket must be a stronger valid modifier");
        return switch (trinket.type()) {
            case TIME -> new TrialPortalModifiers(DATA_VERSION, trinket.value(), skipRooms, insuranceLevel, famePercent);
            case SKIP -> new TrialPortalModifiers(DATA_VERSION, timeMinutes, trinket.value(), insuranceLevel, famePercent);
            case INSURANCE -> new TrialPortalModifiers(DATA_VERSION, timeMinutes, skipRooms, trinket.value(), famePercent);
            case FAME -> new TrialPortalModifiers(DATA_VERSION, timeMinutes, skipRooms, insuranceLevel, trinket.value());
        };
    }

    public int initialTimerTicks() { return 12_000 + timeMinutes * 1_200; }
    public boolean isEmpty() { return timeMinutes == 0 && skipRooms == 0 && insuranceLevel == 0 && famePercent == 0; }

    public long cashoutFame(long base) {
        if (base < 0) throw new IllegalArgumentException("base Fame cannot be negative");
        return switch (famePercent) {
            case 0 -> base;
            case 33 -> Math.multiplyExact(base, 133L) / 100L;
            case 66 -> Math.multiplyExact(base, 166L) / 100L;
            case 100 -> Math.multiplyExact(base, 2L);
            default -> throw new IllegalStateException("Invalid Fame modifier");
        };
    }
}
