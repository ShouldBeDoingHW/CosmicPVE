package com.cosmicpve.economy.flashsale;

public final class FlashSaleRuntime {
    private static final FlashSaleService SERVICE = new FlashSaleService();
    private FlashSaleRuntime() {}
    public static FlashSaleService service() { return SERVICE; }
}
