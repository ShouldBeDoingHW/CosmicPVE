package com.cosmicpve.equipment.enchantment;

/** Final Success/Destroy percentages generated while opening an Unexamined Book. */
public record BookOpeningRates(int successRate, int destroyRate) {
    public BookOpeningRates {
        if (successRate < 1 || successRate > 100 || destroyRate < 1 || destroyRate > 100) {
            throw new IllegalArgumentException("Book-opening rates must be between 1 and 100");
        }
    }
}
