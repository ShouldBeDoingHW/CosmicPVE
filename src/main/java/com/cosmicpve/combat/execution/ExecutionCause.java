package com.cosmicpve.combat.execution;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record ExecutionCause(Identifier id) {
    public ExecutionCause {
        id = Objects.requireNonNull(id);
    }
}
