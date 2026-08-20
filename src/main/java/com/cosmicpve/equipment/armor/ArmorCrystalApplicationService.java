package com.cosmicpve.equipment.armor;

import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.function.IntSupplier;
import net.minecraft.world.item.ItemStack;

/** One atomic server-side crystal transaction. The caller owns slot synchronization. */
public final class ArmorCrystalApplicationService {
    public enum Outcome { SUCCESS, FAILED_DESTROYED, FAILED_PROTECTED, INVALID_CRYSTAL, INVALID_TARGET, ALREADY_SET, UNKNOWN_SET, STALE_TARGET }
    private final CosmicContentRepository content;
    private final IntSupplier rollOneToHundred;

    public ArmorCrystalApplicationService(CosmicContentRepository content, IntSupplier rollOneToHundred) {
        this.content = content;
        this.rollOneToHundred = rollOneToHundred;
    }

    public Outcome apply(ItemStack carried, ItemStack target) {
        return apply(carried, target, target);
    }

    public Outcome apply(ItemStack carried, ItemStack expectedTarget, ItemStack currentTarget) {
        if (expectedTarget != currentTarget) return Outcome.STALE_TARGET;
        ItemStack target = currentTarget;
        if (!carried.is(ModItems.ARMOR_SET_CRYSTAL.get())) return Outcome.INVALID_CRYSTAL;
        var crystal = carried.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
        if (crystal == null || crystal.successRate() < 1 || crystal.successRate() > 100) return Outcome.INVALID_CRYSTAL;
        if (!ArmorSetResolver.isArmor(target)) return Outcome.INVALID_TARGET;
        if (target.has(ModDataComponents.ARMOR_SET_ID.get())) return Outcome.ALREADY_SET;
        var definition = content.findArmorSetDefinition(crystal.identity().setId());
        if (definition.isEmpty()) return Outcome.UNKNOWN_SET;

        boolean success = rollSucceeds(crystal.successRate(), rollOneToHundred);
        carried.shrink(1);
        if (success) {
            target.set(ModDataComponents.ARMOR_SET_ID.get(),
                    com.cosmicpve.data.component.ArmorSetIdentity.from(definition.orElseThrow()));
            return Outcome.SUCCESS;
        }
        if (new com.cosmicpve.equipment.enchantment.WhiteScrollProtectionService().consumeIfProtected(target)) {
            return Outcome.FAILED_PROTECTED;
        }
        target.setCount(0);
        return Outcome.FAILED_DESTROYED;
    }

    public static boolean rollSucceeds(int successRate, IntSupplier rollOneToHundred) {
        if (successRate < 1 || successRate > 100) throw new IllegalArgumentException("successRate must be in [1,100]");
        if (successRate == 100) return true;
        int roll = rollOneToHundred.getAsInt();
        if (roll < 1 || roll > 100) throw new IllegalStateException("Crystal rolls must be in [1,100]");
        return roll <= successRate;
    }
}
