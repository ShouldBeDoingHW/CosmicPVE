package com.cosmicpve.combat.proc;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record ProcProvenance(ProcSourceKind kind, Identifier sourceId) {
    public ProcProvenance {
        kind = Objects.requireNonNull(kind);
        sourceId = Objects.requireNonNull(sourceId);
    }
}
