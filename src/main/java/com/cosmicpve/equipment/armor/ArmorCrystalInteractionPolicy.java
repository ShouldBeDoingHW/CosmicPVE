package com.cosmicpve.equipment.armor;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import java.util.function.Supplier;

/** Decides whether the crystal gesture replaces vanilla slot handling. */
public final class ArmorCrystalInteractionPolicy {
    public enum Decision { VANILLA, APPLY_TO_ARMOR }
    private ArmorCrystalInteractionPolicy() {}

    public static Decision decide(ItemStack carried, ItemStack target, ClickAction action) {
        return decide(carried.has(ModDataComponents.ARMOR_SET_CRYSTAL.get()),
                action == ClickAction.PRIMARY, ArmorSetResolver.isArmor(target));
    }

    static Decision decide(boolean hasCrystalData, boolean primaryClick, boolean armorTarget) {
        return hasCrystalData && primaryClick && armorTarget ? Decision.APPLY_TO_ARMOR : Decision.VANILLA;
    }

    static <T> Optional<T> invokeAuthoritative(
            Decision decision, boolean logicalServer, Supplier<T> applicationAttempt) {
        return decision == Decision.APPLY_TO_ARMOR && logicalServer
                ? Optional.of(applicationAttempt.get()) : Optional.empty();
    }
}
