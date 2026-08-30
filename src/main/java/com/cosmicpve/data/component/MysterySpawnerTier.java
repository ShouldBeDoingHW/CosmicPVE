package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum MysterySpawnerTier implements StringRepresentable {
    SIMPLE(0xFFFFFF), ELITE(0xA3FFF5), MASTERY(0xAA0000);

    public static final Codec<MysterySpawnerTier> CODEC = StringRepresentable.fromEnum(MysterySpawnerTier::values);
    private final int color;

    MysterySpawnerTier(int color) { this.color = color; }
    public int color() { return color; }
    @Override public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
}
