package com.cosmicpve.combat.proc;

@FunctionalInterface
public interface ProcActivationListener {
    void activated(ProcActivation activation);
}
