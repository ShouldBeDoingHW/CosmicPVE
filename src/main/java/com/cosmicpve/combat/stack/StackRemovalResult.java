package com.cosmicpve.combat.stack;

import net.minecraft.resources.Identifier;

public record StackRemovalResult(
        Identifier definitionId,
        int requested,
        int removed,
        int finalCount,
        StackMutationStatus status) {}
