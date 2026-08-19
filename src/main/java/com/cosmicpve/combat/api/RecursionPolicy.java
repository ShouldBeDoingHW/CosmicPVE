package com.cosmicpve.combat.api;

/** Defines which future proc candidates a damage sequence may evaluate. */
public enum RecursionPolicy {
    NORMAL(true, true),
    NO_PROCS(false, false),
    LIMITED_OFFENSIVE_REROLL(true, false);

    private final boolean permitsProcs;
    private final boolean permitsParentProc;

    RecursionPolicy(boolean permitsProcs, boolean permitsParentProc) {
        this.permitsProcs = permitsProcs;
        this.permitsParentProc = permitsParentProc;
    }

    public boolean permitsProcs() {
        return permitsProcs;
    }

    public boolean permitsParentProc() {
        return permitsParentProc;
    }
}
