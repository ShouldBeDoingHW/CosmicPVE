package com.cosmicpve.entity.woodlands;

import java.util.Optional;

/** Seed-testable result of the four independent armor rolls and five bow rolls. */
public record ForestFanaticEquipmentPlan(
        Optional<Helmet> helmet,
        Optional<Chestplate> chestplate,
        Optional<Leggings> leggings,
        Optional<Boots> boots,
        Bow bow) {
    public record Helmet(int protection, int voodoo) {}
    public record Chestplate(int armored, int protection) {}
    public record Leggings(int plagueCarrier, int armored, int protection) {}
    public record Boots(int nimble, int angelic, int armored, int protection) {}
    public record Bow(int venom, int power, int virus, int obliterate, int snare) {}
}
