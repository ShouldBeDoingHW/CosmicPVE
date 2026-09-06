package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Dynamic ordinary-offense and knockback-resistance state for Curse and its Heroic replacement. */
public final class CurseBehavior implements OutgoingDamageContributor {
    public static final Identifier KNOCKBACK_MODIFIER_ID = CosmicPVE.id("curse_knockback_resistance");
    public static final double CURSE_THRESHOLD = 0.30;
    public static final double CURSE_KNOCKBACK = 0.10;
    public static final double FORBIDDEN_DAMAGE = 0.075;
    public static final double FORBIDDEN_KNOCKBACK = 0.15;

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null) return List.of();
        State state = state(context.attacker());
        return state.active() ? List.of(new OutgoingDamageContribution(state.sourceId(), state.outgoingBonus())) : List.of();
    }

    public static State state(LivingEntity wearer) {
        int curse = Math.min(5, EnchantmentLevels.onStack(
                wearer, wearer.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.CURSE));
        int forbidden = Math.min(5, EnchantmentLevels.onStack(
                wearer, wearer.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.FORBIDDEN_CURSE));
        return state(wearer.getHealth(), wearer.getMaxHealth(), curse, forbidden);
    }

    public static State state(double health, double maxHealth, int curseLevel, int forbiddenLevel) {
        if (!(maxHealth > 0.0)) return State.INACTIVE;
        if (forbiddenLevel > 0) {
            int level = Math.min(5, forbiddenLevel);
            boolean active = health / maxHealth < forbiddenThreshold(level);
            return active ? new State(true, ModEnchantments.FORBIDDEN_CURSE.identifier(),
                    FORBIDDEN_DAMAGE, FORBIDDEN_KNOCKBACK) : State.INACTIVE;
        }
        if (curseLevel > 0 && health / maxHealth < CURSE_THRESHOLD) {
            int level = Math.min(5, curseLevel);
            return new State(true, ModEnchantments.CURSE.identifier(), level * 0.01, CURSE_KNOCKBACK);
        }
        return State.INACTIVE;
    }

    public static double forbiddenThreshold(int level) {
        return (30.0 + Math.max(1, Math.min(5, level))) / 100.0;
    }

    public record State(boolean active, Identifier sourceId, double outgoingBonus, double knockbackResistance) {
        private static final State INACTIVE = new State(false, ModEnchantments.CURSE.identifier(), 0.0, 0.0);
    }
}
