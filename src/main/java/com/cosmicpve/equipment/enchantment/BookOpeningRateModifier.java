package com.cosmicpve.equipment.enchantment;

/**
 * Extension seam for future data-backed blocks, buffs, upgrades, or penalties that alter
 * server-generated Unexamined Book rates. Each source owns its own stacking and clamping rules.
 */
@FunctionalInterface
public interface BookOpeningRateModifier {
    BookOpeningRates modify(BookOpeningRateContext context, BookOpeningRates current);
}
