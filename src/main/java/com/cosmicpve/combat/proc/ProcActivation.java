package com.cosmicpve.combat.proc;

public record ProcActivation(ProcEvent event, ProcCandidate candidate, double finalChance, double roll) {}
