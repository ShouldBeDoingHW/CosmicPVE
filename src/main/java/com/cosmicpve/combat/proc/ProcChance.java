package com.cosmicpve.combat.proc;

import java.util.List;

public final class ProcChance {
    private ProcChance() {}

    public static double calculate(double baseProbability, List<Double> multipliers) {
        if (!Double.isFinite(baseProbability) || baseProbability < 0.0) {
            throw new IllegalArgumentException("Base proc probability must be finite and non-negative");
        }
        double chance = baseProbability;
        for (double multiplier : List.copyOf(multipliers)) {
            if (!Double.isFinite(multiplier) || multiplier < 0.0) {
                throw new IllegalArgumentException("Proc chance multipliers must be finite and non-negative");
            }
            chance *= multiplier;
        }
        return Math.clamp(chance, 0.0, 1.0);
    }

    public static double multiplierProduct(List<Double> multipliers) {
        double product = 1.0;
        for (double multiplier : List.copyOf(multipliers)) {
            if (!Double.isFinite(multiplier) || multiplier < 0.0) {
                throw new IllegalArgumentException("Proc chance multipliers must be finite and non-negative");
            }
            product *= multiplier;
        }
        return product;
    }
}
