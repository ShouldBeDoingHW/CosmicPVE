package com.cosmicpve.equipment.enchantment;

public enum CosmicEnchantmentTier {
    SIMPLE(0xFFFFFF, 1, 100, 1, 100),
    UNIQUE(0x55FF55, 1, 100, 1, 100),
    ELITE(0xA3FFF5, 1, 100, 1, 100),
    ULTIMATE(0xFFFF55, 1, 100, 1, 100),
    LEGENDARY(0xFFAA00, 1, 100, 1, 100),
    MASTERY(0xAA0000, 1, 49, 51, 100);

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
    public boolean extractableByBlackScroll() { return this != MASTERY; }
    public boolean allowsRates(int success, int destroy) {
        return success >= minSuccess && success <= maxSuccess && destroy >= minDestroy && destroy <= maxDestroy;
    }
    public int randomSuccess(net.minecraft.util.RandomSource random) { return random.nextIntBetweenInclusive(minSuccess, maxSuccess); }
    public int randomDestroy(net.minecraft.util.RandomSource random) { return random.nextIntBetweenInclusive(minDestroy, maxDestroy); }
}
