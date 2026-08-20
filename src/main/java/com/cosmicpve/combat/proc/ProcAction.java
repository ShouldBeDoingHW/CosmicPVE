package com.cosmicpve.combat.proc;

@FunctionalInterface
public interface ProcAction {
    void execute(ProcActivation activation);
}
