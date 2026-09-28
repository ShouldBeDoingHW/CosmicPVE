package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.ownership.GeneralAllyResolver;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/** Evaluates nearby enemies at the moment an ordinary root attack is calculated. */
public final class AntiGankBehavior implements OutgoingDamageContributor {
    public static final double RADIUS = 10.0;
    private final GeneralAllyResolver allies;

    public AntiGankBehavior(GeneralAllyResolver allies) { this.allies = allies; }

    public static double bonus(int level, int nearbyEnemies) {
        return Math.min(Math.max(0, nearbyEnemies), 3 * Math.max(0, Math.min(4, level))) * .01;
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        LivingEntity attacker = context.attacker();
        if (context.channel() != DamageChannel.ORDINARY || attacker == null
                || (context.category() != AttackCategory.MELEE && context.category() != AttackCategory.PROJECTILE))
            return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.ANTI_GANK.identifier());
        if (level <= 0) return List.of();
        int enemies = 0;
        for (LivingEntity candidate : attacker.level().getEntitiesOfClass(LivingEntity.class,
                attacker.getBoundingBox().inflate(RADIUS), entity -> entity != attacker && entity.isAlive()
                        && !entity.isDeadOrDying() && !entity.isRemoved())) {
            if (GeneralAllyResolver.within(attacker, candidate, RADIUS) && !allies.isAlly(attacker, candidate)) {
                enemies++;
            }
        }
        double value = bonus(level, enemies);
        return value == 0.0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.ANTI_GANK.identifier(), value));
    }
}
