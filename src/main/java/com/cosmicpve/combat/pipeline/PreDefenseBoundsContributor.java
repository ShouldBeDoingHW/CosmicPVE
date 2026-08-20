package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageBounds;

/** Resolves ordinary-damage bounds after outgoing math and before incoming defenses. */
@FunctionalInterface
public interface PreDefenseBoundsContributor {
    DamageBounds resolvePreDefenseBounds(CombatContext context);
}
