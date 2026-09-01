package com.cosmicpve.equipment.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum CosmicEnchantmentTier {
    SIMPLE(0xFFFFFF, 1, 100, 1, 100),
    UNIQUE(0x55FF55, 1, 100, 1, 100),
    ELITE(0xA3FFF5, 1, 100, 1, 100),
    ULTIMATE(0xFFFF55, 1, 100, 1, 100),
    LEGENDARY(0xFFAA00, 1, 100, 1, 100),
    MASTERY(0xAA0000, 1, 49, 51, 100),
    HEROIC(0xFF00A2, 1, 100, 1, 100);

    public static final Codec<CosmicEnchantmentTier> CODEC = Codec.STRING.comapFlatMap(name -> {
        try {
            return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown Cosmic enchantment tier: " + name);
        }
    }, tier -> tier.name().toLowerCase(Locale.ROOT));

    private final int tooltipColor;
    private final int minSuccess, maxSuccess, minDestroy, maxDestroy;
    CosmicEnchantmentTier(int tooltipColor, int minSuccess, int maxSuccess, int minDestroy, int maxDestroy) {
        this.tooltipColor = tooltipColor;
        this.minSuccess = minSuccess;
        this.maxSuccess = maxSuccess;
        this.minDestroy = minDestroy;
        this.maxDestroy = maxDestroy;
    }
    public int tooltipColor() { return tooltipColor; }
    public String serializedName() { return name().toLowerCase(Locale.ROOT); }
    public boolean extractableByBlackScroll() { return this != MASTERY && this != HEROIC; }
    public boolean allowsRates(int success, int destroy) {
        return success >= minSuccess && success <= maxSuccess && destroy >= minDestroy && destroy <= maxDestroy;
    }
    public int randomSuccess(net.minecraft.util.RandomSource random) { return random.nextIntBetweenInclusive(minSuccess, maxSuccess); }
    public int randomDestroy(net.minecraft.util.RandomSource random) { return random.nextIntBetweenInclusive(minDestroy, maxDestroy); }
}
