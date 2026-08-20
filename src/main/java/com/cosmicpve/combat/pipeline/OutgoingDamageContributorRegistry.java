package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Composition point for enchantments and later equipment systems contributing ordinary offense. */
public final class OutgoingDamageContributorRegistry implements OutgoingDamageContributor {
    private final CopyOnWriteArrayList<OutgoingDamageContributor> contributors = new CopyOnWriteArrayList<>();

    public void register(OutgoingDamageContributor contributor) {
        contributors.addIfAbsent(contributor);
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        var result = new ArrayList<OutgoingDamageContribution>();
        for (var contributor : contributors) {
            result.addAll(contributor.resolve(context));
        }
        return List.copyOf(result);
    }
}
