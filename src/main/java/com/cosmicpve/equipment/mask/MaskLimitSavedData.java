package com.cosmicpve.equipment.mask;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent world-wide policy for creating new Multi-Masks. */
public final class MaskLimitSavedData extends SavedData {
    public static final int DEFAULT_LIMIT = 3;
    public static final int MIN_LIMIT = 2;
    public static final int MAX_LIMIT = 5;
    public static final Codec<MaskLimitSavedData> CODEC = Codec.intRange(MIN_LIMIT, MAX_LIMIT)
            .optionalFieldOf("mask_limit", DEFAULT_LIMIT).codec()
            .xmap(MaskLimitSavedData::new, MaskLimitSavedData::limit);
    private int limit;

    public MaskLimitSavedData() { this(DEFAULT_LIMIT); }
    private MaskLimitSavedData(int limit) { this.limit = limit; }
    public int limit() { return limit; }
    public void setLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) throw new IllegalArgumentException("Mask limit must be 2-5");
        if (this.limit != limit) { this.limit = limit; setDirty(); }
    }
}
