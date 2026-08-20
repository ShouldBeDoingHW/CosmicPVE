package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import java.util.List;

public final class ArmorSetCombatContributor implements OutgoingDamageContributor, IncomingDamageContributor {
    private final ArmorSetResolver sets;

    public ArmorSetCombatContributor(ArmorSetResolver sets) { this.sets = sets; }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null) return List.of();
        return sets.resolve(context.attacker())
                .filter(definition -> definition.additiveOutgoingBonus() != 0.0)
                .map(definition -> List.of(new OutgoingDamageContribution(
                        definition.id(), definition.id().equals(ArmorSetIds.ANCIENT)
                                ? AncientArmorSetBehavior.outgoing(
                                        context.attacker().getHealth(), context.attacker().getMaxHealth())
                                : definition.additiveOutgoingBonus())))
                .orElseGet(List::of);
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        return sets.resolve(context.target())
                .filter(definition -> definition.incomingMultiplier() != 1.0)
                .map(definition -> List.of(new IncomingDamageContribution(
                        definition.id(), definition.id().equals(ArmorSetIds.ANCIENT)
                                ? AncientArmorSetBehavior.incoming(
                                        context.target().getHealth(), context.target().getMaxHealth())
                                : definition.incomingMultiplier())))
                .orElseGet(List::of);
    }
}
