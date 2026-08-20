package com.cosmicpve.combat.proc;

import net.minecraft.util.RandomSource;

@FunctionalInterface
public interface ProcRandomSource {
    double nextDouble();

    static ProcRandomSource server(RandomSource random) {
        return random::nextDouble;
    }
}
