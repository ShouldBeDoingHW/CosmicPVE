package com.cosmicpve.equipment.armor;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import java.util.List;

public final class ArmorSetCombatContributor implements OutgoingDamageContributor, IncomingDamageContributor {
    private final ArmorSetResolver sets;
    private final com.cosmicpve.activity.ActivityContextService activities;

    public ArmorSetCombatContributor(ArmorSetResolver sets) {
        this(sets, new com.cosmicpve.activity.ActivityContextService());
    }
    public ArmorSetCombatContributor(ArmorSetResolver sets, com.cosmicpve.activity.ActivityContextService activities) {
        this.sets = sets; this.activities = activities;
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null) return List.of();
        return sets.resolve(context.attacker())
                .map(definition -> List.of(new OutgoingDamageContribution(
                        definition.id(), outgoingBonus(definition, context))))
                .filter(list -> list.getFirst().bonus() != 0.0)
                .orElseGet(List::of);
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        return sets.resolve(context.target())
                .map(definition -> List.of(new IncomingDamageContribution(
                        definition.id(), incomingMultiplier(definition.id(), definition.incomingMultiplier(),
                                context.target()))))
                .filter(list -> list.getFirst().multiplier() != 1.0)
                .orElseGet(List::of);
    }

    private double outgoingBonus(com.cosmicpve.content.definition.armor.ArmorSetDefinition definition,
            CombatContext context) {
        if (definition.id().equals(ArmorSetIds.ANCIENT))
            return AncientArmorSetBehavior.outgoing(context.attacker().getHealth(), context.attacker().getMaxHealth());
        if (definition.id().equals(ArmorSetIds.YJIKI))
            return activities.isDungeon(context.attacker()) ? .10 : .05;
        if (definition.id().equals(ArmorSetIds.RANGER))
            return isBowProjectile(context) ? .20 : 0.0;
        return definition.additiveOutgoingBonus();
    }

    private double incomingMultiplier(net.minecraft.resources.Identifier id, double configured,
            net.minecraft.world.entity.LivingEntity target) {
        if (id.equals(ArmorSetIds.ANCIENT))
            return AncientArmorSetBehavior.incoming(target.getHealth(), target.getMaxHealth());
        if (id.equals(ArmorSetIds.YJIKI)) return activities.isDungeon(target) ? .70 : .85;
        return configured;
    }

    public static boolean isBowProjectile(CombatContext context) {
        if (context.category() != com.cosmicpve.combat.api.AttackCategory.PROJECTILE) return false;
        var stack = context.weaponSnapshot().stack();
        return stack.is(net.minecraft.world.item.Items.BOW) || stack.is(net.minecraft.world.item.Items.CROSSBOW);
    }
}
