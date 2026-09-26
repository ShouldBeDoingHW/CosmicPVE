package com.cosmicpve.equipment.accessory;

import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.registry.ModEnchantments;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Additive boost to the Luck factor, applied centrally after other modifier resolution. */
public final class CinderwolfProcFactor {
    public static final double BONUS = .15;
    public static final Set<Identifier> ELIGIBLE = Set.of(
            ModEnchantments.DIVINE_IMMOLATION.identifier(),
            ModEnchantments.MOLTEN.identifier(), ModEnchantments.PYRE.identifier());
    private static final AccessoryResolver ACCESSORIES = new AccessoryResolver();

    private CinderwolfProcFactor() {}

    public static double boostedLuckFactor(double normalLuckFactor) {
        if (!Double.isFinite(normalLuckFactor) || normalLuckFactor <= 0.0)
            throw new IllegalArgumentException("Luck factor must be positive and finite");
        return normalLuckFactor + BONUS;
    }

    public static double multiplier(ProcEvent event, ProcCandidate candidate) {
        if (!ELIGIBLE.contains(candidate.effectId())) return 1.0;
        LivingEntity owner = candidate.effectId().equals(ModEnchantments.MOLTEN.identifier())
                ? event.target() : event.attacker();
        if (owner == null || !ACCESSORIES.hasBelt(owner, BeltDefinition.CINDERWOLF)) return 1.0;
        double normalLuck = event.namedChanceMultipliers().getOrDefault(ModEnchantments.LUCK.identifier(), 1.0);
        return boostedLuckFactor(normalLuck) / normalLuck;
    }
}
