package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageBounds;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Combines independent bounds using the strongest floor and lowest cap. */
public final class PreDefenseBoundsContributorRegistry implements PreDefenseBoundsContributor {
    private final List<PreDefenseBoundsContributor> contributors = new CopyOnWriteArrayList<>();

    public void register(PreDefenseBoundsContributor contributor) {
        contributors.add(contributor);
    }

    @Override
    public DamageBounds resolvePreDefenseBounds(CombatContext context) {
        double floor = 0.0;
        double cap = Double.POSITIVE_INFINITY;
        for (var contributor : contributors) {
            var bounds = contributor.resolvePreDefenseBounds(context);
            floor = Math.max(floor, bounds.floor());
            cap = Math.min(cap, bounds.cap());
        }
        return new DamageBounds(Math.min(floor, cap), cap);
    }
}
