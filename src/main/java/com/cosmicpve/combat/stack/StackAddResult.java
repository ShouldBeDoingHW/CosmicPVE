package com.cosmicpve.combat.stack;

import net.minecraft.resources.Identifier;

public record StackAddResult(
        Identifier definitionId,
        int requested,
        int added,
        int refreshed,
        int finalCount,
        StackMutationStatus status) {}
