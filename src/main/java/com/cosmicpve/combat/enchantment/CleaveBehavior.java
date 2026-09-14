package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.ownership.GeneralAllyResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Shared Cleave/Mighty Cleave calculation and ordinary-child delivery. */
public final class CleaveBehavior {
    public static final double CLEAVE_RADIUS = 6.0;
    public static final double MIGHTY_RADIUS = 7.0;
    public static final double MIGHTY_HEAL_RADIUS = 4.0;
    public static final float MIGHTY_HEAL_HP = 1.0F;
    public static final Set<Identifier> RECURSION_EXCLUSIONS = Set.of(
            ModEnchantments.CLEAVE.identifier(), ModEnchantments.MIGHTY_CLEAVE.identifier());

    private final GeneralAllyResolver allies;
    private final ChildCombatActionService childActions;

    public CleaveBehavior(GeneralAllyResolver allies, ChildCombatActionService childActions) {
        this.allies = allies;
        this.childActions = childActions;
    }

    public static double cleaveChance(int level) { return Math.max(0, Math.min(8, level)) * 0.01; }
    public static double mightyChance() { return 0.08; }
    public static double cleaveFraction(int level) { return (10.0 + Math.max(0, Math.min(8, level))) / 100.0; }
    public static double mightyFraction() { return 0.20; }
    public static double childDamage(double finalizedOrdinaryDamage, int level, boolean mighty) {
        if (!Double.isFinite(finalizedOrdinaryDamage) || finalizedOrdinaryDamage < 0.0)
            throw new IllegalArgumentException("Invalid parent ordinary damage");
        return finalizedOrdinaryDamage * (mighty ? mightyFraction() : cleaveFraction(level));
    }

    public Set<LivingEntity> activate(CombatResult parent, int level, boolean mighty) {
        if (!parent.isCommittedDamagingHit() || parent.context().channel() != DamageChannel.ORDINARY
                || parent.context().attacker() == null || parent.context().target() == null) return Set.of();
        LivingEntity attacker = parent.context().attacker();
        LivingEntity origin = parent.context().target();
        double amount = childDamage(parent.breakdown().finalOrdinaryDamage(), level, mighty);
        double radius = mighty ? MIGHTY_RADIUS : CLEAVE_RADIUS;
        var delivered = new LinkedHashSet<LivingEntity>();
        var affected = new LinkedHashSet<LivingEntity>();
        for (LivingEntity candidate : origin.level().getEntitiesOfClass(LivingEntity.class,
                origin.getBoundingBox().inflate(radius), entity -> entity != attacker && entity.isAlive()
                        && !entity.isRemoved() && !allies.isAlly(attacker, entity))) {
            if (!GeneralAllyResolver.within(origin, candidate, radius) || !delivered.add(candidate)) continue;
            var outcome = candidate == origin
                    ? childActions.deliverCleave(parent.context(), candidate, amount, RECURSION_EXCLUSIONS)
                    : childActions.deliverOrdinary(parent.context(), candidate, amount,
                        RecursionPolicy.LIMITED_OFFENSIVE_REROLL, RECURSION_EXCLUSIONS);
            if (outcome.accepted() && outcome.healthDamage() > 0) affected.add(candidate);
        }
        if (mighty) {
            for (LivingEntity ally : allies.alliesWithin(attacker, MIGHTY_HEAL_RADIUS)) {
                if (ally.getHealth() < ally.getMaxHealth()) ally.heal(MIGHTY_HEAL_HP);
            }
        }
        return Set.copyOf(affected);
    }
}
