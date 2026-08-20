package com.cosmicpve.combat.stack;

/** scanned=false is the cheap path used when an entity has no due timed stacks. */
public record StackExpirationResult(int expired, boolean scanned) {}
