package com.cosmicpve.combat.api;

public record DamageBounds(double floor, double cap) {
    public static final DamageBounds UNBOUNDED = new DamageBounds(0.0, Double.POSITIVE_INFINITY);

    public DamageBounds {
        if (!Double.isFinite(floor) || floor < 0.0 || Double.isNaN(cap) || cap < floor) {
            throw new IllegalArgumentException("Damage bounds require 0 <= floor <= cap");
        }
    }

    public double apply(double value) {
        return Math.min(cap, Math.max(floor, value));
    }
}
