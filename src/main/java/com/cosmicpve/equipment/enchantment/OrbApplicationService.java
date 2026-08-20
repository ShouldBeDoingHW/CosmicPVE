package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.registry.ModDataComponents;
import java.util.OptionalInt;
import java.util.function.IntSupplier;
import net.minecraft.world.item.ItemStack;

public final class OrbApplicationService {
    private final CustomEnchantCapacityService capacity;
    private final WhiteScrollProtectionService protection;
    private final IntSupplier roll;

    public OrbApplicationService(CustomEnchantCapacityService capacity,
            WhiteScrollProtectionService protection, IntSupplier roll) {
        this.capacity = capacity;
        this.protection = protection;
        this.roll = roll;
    }

    public OrbApplicationResult apply(ItemStack orb, ItemStack expected, ItemStack target) {
        var type = OrbType.fromStack(orb).orElse(null);
        var data = orb.get(ModDataComponents.ENCHANTMENT_ORB.get());
        int before = capacity.capacity(target);
        boolean protectedBefore = protection.isProtected(target);
        if (expected != target) return result(OrbApplicationResult.Outcome.STALE_TARGET, type, data, before, before, protectedBefore);
        if (type == null || data == null) return result(OrbApplicationResult.Outcome.REJECTED_INVALID_ORB, type, data, before, before, protectedBefore);
        if (!type.matches(target)) return result(OrbApplicationResult.Outcome.REJECTED_TARGET, type, data, before, before, protectedBefore);
        if (!capacity.canUpgrade(target, type)) return result(OrbApplicationResult.Outcome.REJECTED_MAX_CAPACITY, type, data, before, before, protectedBefore);

        var decision = CosmicBookRollResolver.resolve(data.successRate(), data.destroyRate(), roll);
        orb.shrink(1);
        if (decision.outcome() == CosmicBookRollResolver.Outcome.SUCCESS) {
            capacity.incrementOrbUpgrade(target, type);
            return result(OrbApplicationResult.Outcome.SUCCESS, type, data, before, capacity.capacity(target),
                    protectedBefore, decision.successRoll(), null);
        }
        int destroyRoll = decision.destroyRoll().orElseThrow();
        if (decision.outcome() == CosmicBookRollResolver.Outcome.FAILED_SURVIVED) {
            return result(OrbApplicationResult.Outcome.FAILED_SURVIVED, type, data, before, before,
                    protectedBefore, decision.successRoll(), destroyRoll);
        }
        if (protection.consumeIfProtected(target)) {
            return result(OrbApplicationResult.Outcome.FAILED_PROTECTED, type, data, before, before,
                    protectedBefore, decision.successRoll(), destroyRoll);
        }
        target.setCount(0);
        return result(OrbApplicationResult.Outcome.FAILED_DESTROYED, type, data, before, 0,
                protectedBefore, decision.successRoll(), destroyRoll);
    }

    private static OrbApplicationResult result(OrbApplicationResult.Outcome outcome, OrbType type,
            EnchantmentOrbData data, int before, int after, boolean protectedBefore) {
        return result(outcome, type, data, before, after, protectedBefore, null, null);
    }

    private static OrbApplicationResult result(OrbApplicationResult.Outcome outcome, OrbType type,
            EnchantmentOrbData data, int before, int after, boolean protectedBefore, Integer successRoll, Integer destroyRoll) {
        return new OrbApplicationResult(outcome, type, data == null ? 0 : data.successRate(),
                successRoll == null ? OptionalInt.empty() : OptionalInt.of(successRoll),
                data == null ? 0 : data.destroyRate(),
                destroyRoll == null ? OptionalInt.empty() : OptionalInt.of(destroyRoll),
                before, after, protectedBefore);
    }
}
