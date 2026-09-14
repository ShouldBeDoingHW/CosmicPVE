package com.cosmicpve.combat.proc;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;

/** A successful roll plus the entities its action actually affected. Actions report resolution before listeners run. */
public final class ProcActivation {
    private final ProcEvent event;
    private final ProcCandidate candidate;
    private final double finalChance;
    private final double roll;
    private final Set<LivingEntity> affectedEntities = Collections.newSetFromMap(new IdentityHashMap<>());
    public ProcActivation(ProcEvent event, ProcCandidate candidate, double finalChance, double roll) {
        this.event = event; this.candidate = candidate; this.finalChance = finalChance; this.roll = roll;
    }
    public ProcEvent event() { return event; }
    public ProcCandidate candidate() { return candidate; }
    public double finalChance() { return finalChance; }
    public double roll() { return roll; }
    public void markAffected(LivingEntity entity) { if (entity != null) affectedEntities.add(entity); }
    public Set<LivingEntity> affectedEntities() { return Set.copyOf(affectedEntities); }
}
