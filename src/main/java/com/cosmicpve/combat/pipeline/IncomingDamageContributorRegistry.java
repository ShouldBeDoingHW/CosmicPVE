package com.cosmicpve.combat.pipeline;

import com.cosmicpve.combat.api.CombatContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class IncomingDamageContributorRegistry implements IncomingDamageContributor {
    private final List<IncomingDamageContributor> contributors = new CopyOnWriteArrayList<>();

    public void register(IncomingDamageContributor contributor) { contributors.add(contributor); }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        var resolved = new ArrayList<IncomingDamageContribution>();
        contributors.forEach(source -> resolved.addAll(source.resolveIncoming(context)));
        return List.copyOf(resolved);
    }
}
