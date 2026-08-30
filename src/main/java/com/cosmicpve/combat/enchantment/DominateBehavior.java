package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModMobEffects;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class DominateBehavior implements OutgoingDamageContributor {
    public static final double BASE_CHANCE = 0.20;

    public static double chance(int level) { return level <= 0 ? 0.0 : BASE_CHANCE; }
    public static double reduction(int level) { return 0.03 * Math.max(0, Math.min(4, level)); }
    public static int durationTicks(int level) { return 20 * Math.max(1, Math.min(4, level)); }
    public static int strongestLevel(int existingLevel, int appliedLevel) {
        return Math.max(Math.max(0, Math.min(4, existingLevel)), Math.max(1, Math.min(4, appliedLevel)));
    }

    public static boolean qualifies(AttackCategory category, boolean hasTarget) {
        return hasTarget && category == AttackCategory.PROJECTILE;
    }

    public static boolean eligible(ProcEvent event) {
        return event.combatResult()
                .map(result -> qualifies(result.context().category(), event.target() != null))
                .orElse(false);
    }

    public static void activate(LivingEntity target, int level) {
        if (target.isDeadOrDying()) return;
        MobEffectInstance existing = target.getEffect(ModMobEffects.DOMINATED);
        int strongest = strongestLevel(existing == null ? 0 : existing.getAmplifier() + 1, level);
        target.removeEffect(ModMobEffects.DOMINATED);
        target.addEffect(new MobEffectInstance(ModMobEffects.DOMINATED, durationTicks(strongest), strongest - 1));
    }

    public static int activeLevel(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModMobEffects.DOMINATED);
        return effect == null ? 0 : Math.min(4, effect.getAmplifier() + 1);
    }

    public static List<OutgoingDamageContribution> contribution(DamageChannel channel, int level) {
        return channel != DamageChannel.ORDINARY || level <= 0 ? List.of()
                : List.of(new OutgoingDamageContribution(ModEnchantments.DOMINATE.identifier(), -reduction(level)));
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        return contribution(context.channel(), activeLevel(context.attacker()));
    }
}
