package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class MightyCactusBehavior {
    public static final double TRUE_DAMAGE = 3.0;
    public static final RecursionPolicy RECURSION_POLICY = RecursionPolicy.NO_PROCS;
    private MightyCactusBehavior() {}
    public static int equippedLevel(LivingEntity wearer) {
        return EnchantmentLevels.onStack(wearer.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.MIGHTY_CACTUS);
    }
    public static double chance(int level) { return Math.min(1.0, .04 * Math.max(0, Math.min(2, level))); }
    public static TrueDamagePacket packet() { return TrueDamagePacket.standard(ModEnchantments.MIGHTY_CACTUS.identifier(), TRUE_DAMAGE); }
}
