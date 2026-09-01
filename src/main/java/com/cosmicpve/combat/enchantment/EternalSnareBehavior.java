package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.item.CrossbowItem;

public final class EternalSnareBehavior implements OutgoingDamageContributor {
    public static final int DURATION_TICKS = 35;
    public static final double MELEE_VULNERABILITY = .15;
    private final SnareRootService roots;
    public EternalSnareBehavior(SnareRootService roots) { this.roots = roots; }
    public static double chance(int level) { return .04 * Math.max(0, Math.min(4, level)); }
    public static boolean eligible(ProcEvent event) {
        return event.combatResult().map(result -> result.context().category() == AttackCategory.PROJECTILE
                && result.context().weaponSnapshot().stack().getItem() instanceof CrossbowItem).orElse(false);
    }
    public void activate(ProcEvent event) {
        if (event.target() != null) roots.apply(event.target(), event.serverTick(), DURATION_TICKS, MELEE_VULNERABILITY);
    }
    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.category() != AttackCategory.MELEE
                || context.target() == null || context.target().level().getServer() == null) return List.of();
        double value = roots.meleeVulnerability(context.target().getUUID(),
                context.target().level().getServer().getTickCount());
        return value <= 0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.ETERNAL_SNARE.identifier(), value));
    }
}
