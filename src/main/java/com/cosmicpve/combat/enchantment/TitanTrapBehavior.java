package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

/** The already-trapped target is read during the ordinary damage calculation, before this hit's proc. */
public final class TitanTrapBehavior implements OutgoingDamageContributor {
    private final CombatStackService stacks;
    public TitanTrapBehavior(CombatStackService stacks) { this.stacks = stacks; }

    public static double bonus(int level) { return Math.max(0, Math.min(3, level)) * .04; }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.category() != AttackCategory.MELEE
                || context.attacker() == null || context.target() == null
                || context.target().level().getServer() == null) return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.TITAN_TRAP.identifier());
        long tick = context.target().level().getServer().getTickCount();
        return level <= 0 || stacks.count(context.target(), TrapBehavior.STACK_ID, tick) == 0
                ? List.of() : List.of(new OutgoingDamageContribution(
                        ModEnchantments.TITAN_TRAP.identifier(), bonus(level)));
    }
}
