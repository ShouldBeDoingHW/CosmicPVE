package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatContext;
import java.util.List;

@FunctionalInterface
public interface OutgoingDamageContributor {
    List<OutgoingDamageContribution> resolve(CombatContext context);
}
