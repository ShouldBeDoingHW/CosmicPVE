package com.cosmicpve.combat.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class PaladinArmoredBehavior {
    public static final int WEAKNESS_TICKS = 50;
    private PaladinArmoredBehavior() {}
    public static int equippedLevelTotal(LivingEntity wearer) {
        int total = 0;
        for (var slot : new net.minecraft.world.entity.EquipmentSlot[] {
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET }) {
            total += EnchantmentLevels.onStack(wearer.getItemBySlot(slot), ModEnchantments.PALADIN_ARMORED);
        }
        return Math.min(16, total);
    }
    public static double chance(int totalLevel) { return Math.max(0, Math.min(16, totalLevel)) / 100.0; }
    public static void weaken(LivingEntity attacker) {
        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));
    }
}
