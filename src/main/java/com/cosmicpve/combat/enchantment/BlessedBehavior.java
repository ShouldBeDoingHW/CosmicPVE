package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.content.definition.stack.StackPolarity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class BlessedBehavior {
    private BlessedBehavior() {}
    public static double chance(int level) { return 0.02 * level; }

    public static List<CleanseTarget> eligible(LivingEntity attacker, CombatStackService stacks, long tick) {
        var result = new ArrayList<CleanseTarget>();
        for (var active : stacks.byPolarity(attacker, StackPolarity.NEGATIVE, tick)) {
            if (active.definition().map(definition -> definition.cleansable()).orElse(false)) {
                for (int i = 0; i < active.count(); i++) result.add(new StackTarget(active.definitionId()));
            }
        }
        attacker.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .forEach(effect -> result.add(new EffectTarget(effect.getEffect())));
        return List.copyOf(result);
    }

    public static void activate(ProcEvent event, CombatStackService stacks) {
        LivingEntity attacker = event.attacker();
        if (attacker == null) return;
        List<CleanseTarget> pool = eligible(attacker, stacks, event.serverTick());
        if (pool.isEmpty()) return;
        int index = Math.min(pool.size() - 1, (int) Math.floor(event.random().nextDouble() * pool.size()));
        pool.get(index).remove(attacker, stacks, event.serverTick());
    }

    public sealed interface CleanseTarget permits StackTarget, EffectTarget {
        void remove(LivingEntity entity, CombatStackService stacks, long tick);
    }
    public record StackTarget(Identifier definitionId) implements CleanseTarget {
        @Override public void remove(LivingEntity entity, CombatStackService stacks, long tick) {
            stacks.removeOne(entity, definitionId, tick);
        }
    }
    public record EffectTarget(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) implements CleanseTarget {
        @Override public void remove(LivingEntity entity, CombatStackService stacks, long tick) { entity.removeEffect(effect); }
    }
}
