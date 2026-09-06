package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class CactusBehavior {
    public static final double CHANCE_PER_LEVEL = 0.03;
    public static final double TRUE_DAMAGE = 1.5;
    public static final RecursionPolicy RECURSION_POLICY = RecursionPolicy.NO_PROCS;
    private CactusBehavior() {}
    public static int equippedLevel(LivingEntity wearer) {
        return EnchantmentLevels.onStack(wearer, wearer.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.CACTUS);
    }
    public static double chance(int level) { return Math.min(1.0, CHANCE_PER_LEVEL * Math.max(0, level)); }
    public static TrueDamagePacket packet() { return TrueDamagePacket.standard(ModEnchantments.CACTUS.identifier(), TRUE_DAMAGE); }
}
