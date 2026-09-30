package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;

/** Evaluates the two current health values before ordinary melee damage is applied. */
public final class BossSlayerBehavior implements OutgoingDamageContributor {
    public static boolean qualifies(double attackerHealth, double targetHealth) {
        return attackerHealth > 0 && targetHealth >= attackerHealth * 3.0;
    }

    public static double bonus(int level) { return Math.max(0, Math.min(3, level)) * .03; }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.category() != AttackCategory.MELEE
                || context.attacker() == null || context.target() == null
                || !(context.weaponSnapshot().stack().is(ItemTags.SWORDS)
                        || context.weaponSnapshot().stack().getItem() instanceof AxeItem)
                || !qualifies(context.attacker().getHealth(), context.target().getHealth())) return List.of();
        int level = context.effectiveEnchantments().level(ModEnchantments.BOSS_SLAYER.identifier());
        return level <= 0 ? List.of() : List.of(new OutgoingDamageContribution(
                ModEnchantments.BOSS_SLAYER.identifier(), bonus(level)));
    }
}
