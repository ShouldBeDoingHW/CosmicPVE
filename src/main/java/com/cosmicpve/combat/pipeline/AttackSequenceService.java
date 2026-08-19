package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.AttackSequence;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

public final class AttackSequenceService {
    private final AtomicLong nextId;

    public AttackSequenceService() {
        this(1L);
    }

    AttackSequenceService(long firstId) {
        nextId = new AtomicLong(firstId);
    }

    public AttackSequence nextRoot() {
        return new AttackSequence(nextId.getAndIncrement(), OptionalLong.empty());
    }

    public AttackSequence nextChild(long parentId) {
        if (parentId <= 0) {
            throw new IllegalArgumentException("Parent sequence IDs must be positive");
        }
        return new AttackSequence(nextId.getAndIncrement(), OptionalLong.of(parentId));
    }
}
