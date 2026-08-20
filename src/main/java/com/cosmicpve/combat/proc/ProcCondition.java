package com.cosmicpve.combat.proc;

import net.minecraft.resources.Identifier;

public interface ProcCondition {
    Identifier id();

    boolean matches(ProcEvent event);
}
