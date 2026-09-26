package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatResult;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.ownership.GeneralAllyResolver;
import com.cosmicpve.CosmicPVE;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;

/** Copies one committed ordinary parent snapshot to nearby non-allies, never to an offensive parent. */
public final class DeathCoffinBehavior {
    private static final TagKey<Item> ALL_WEAPONS = TagKey.create(Registries.ITEM, CosmicPVE.id("enchantable/all_weapons"));
    private final GeneralAllyResolver allies;
    private final ChildCombatActionService children;

    public DeathCoffinBehavior(GeneralAllyResolver allies, ChildCombatActionService children) {
        this.allies = allies;
        this.children = children;
    }

    public static boolean belowThreshold(double redHealth, double maximumHealth) {
        return redHealth > 0.0 && maximumHealth > 0.0 && redHealth / maximumHealth < .33;
    }

    public static int radius(int level) { return Math.max(0, Math.min(3, level)); }

    /** Execute-style threshold is assessed on the target's red health at parent-hit entry. */
    public static boolean parentTargetWasBelowThreshold(CombatResult parent) {
        var target = parent.context().target();
        return target != null && belowThreshold(
                target.getHealth() + parent.committedHealthDamage(), target.getMaxHealth());
    }

    public static boolean eligible(CombatResult parent) {
        if (!parent.isCommittedDamagingHit() || parent.context().channel() != DamageChannel.ORDINARY
                || parent.context().parentSequenceId().isPresent() || parent.context().attacker() == null
                || parent.context().target() == null) return false;
        var context = parent.context();
        var weapon = context.weaponSnapshot().stack();
        return (context.category() == AttackCategory.MELEE || context.category() == AttackCategory.PROJECTILE)
                && weapon.is(ALL_WEAPONS)
                && parentTargetWasBelowThreshold(parent);
    }

    public Set<LivingEntity> activate(CombatResult parent, int level) {
        if (!eligible(parent) || radius(level) == 0) return Set.of();
        LivingEntity attacker = parent.context().attacker();
        LivingEntity origin = parent.context().target();
        double amount = parent.breakdown().finalOrdinaryDamage();
        if (amount <= 0.0) return Set.of();
        int radius = radius(level);
        var affected = new LinkedHashSet<LivingEntity>();
        for (LivingEntity candidate : origin.level().getEntitiesOfClass(LivingEntity.class,
                origin.getBoundingBox().inflate(radius), entity -> entity != attacker && entity != origin
                        && entity.isAlive() && !entity.isRemoved() && !allies.isAlly(attacker, entity))) {
            if (!GeneralAllyResolver.within(origin, candidate, radius)) continue;
            var outcome = children.deliverOrdinary(parent.context(), candidate, amount, RecursionPolicy.NO_PROCS);
            if (outcome.accepted() && outcome.healthDamage() > 0.0) affected.add(candidate);
        }
        return Set.copyOf(affected);
    }
}
