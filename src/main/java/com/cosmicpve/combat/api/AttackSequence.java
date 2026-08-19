package com.cosmicpve.combat.api;

import java.util.OptionalLong;

public record AttackSequence(long id, OptionalLong parentId) {
    public AttackSequence {
        if (id <= 0) {
            throw new IllegalArgumentException("Attack sequence IDs must be positive");
        }
        parentId = parentId == null ? OptionalLong.empty() : parentId;
    }
}
