package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class EnderWalkerBehavior implements IncomingDamageContributor {
    public static double multiplier(int level) { return 1.0 - Math.max(0, Math.min(5, level)) * .10; }
    public static boolean protectedSource(net.minecraft.world.damagesource.DamageSource source) {
        return source != null && (source.is(DamageTypes.WITHER) || source.is(NeoForgeMod.POISON_DAMAGE));
    }
    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.target() == null
                || !protectedSource(context.damageSource())) return List.of();
        int level = EnchantmentLevels.onStack(context.target(), context.target().getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET), ModEnchantments.ENDER_WALKER);
        return level <= 0 ? List.of() : List.of(new IncomingDamageContribution(
                ModEnchantments.ENDER_WALKER.identifier(), multiplier(level)));
    }
}
