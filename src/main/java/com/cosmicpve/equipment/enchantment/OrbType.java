package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModItems;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public enum OrbType {
    ARMOR(CustomEnchantCapacityService.ARMOR_MAX_ORB_BONUS),
    WEAPON(CustomEnchantCapacityService.WEAPON_MAX_ORB_BONUS);

    private final int maximumBonus;
    OrbType(int maximumBonus) { this.maximumBonus = maximumBonus; }
    public int maximumBonus() { return maximumBonus; }
    public int maximumCapacity() { return CustomEnchantCapacityService.BASE_CAPACITY + maximumBonus; }
    public boolean canUpgradeBonus(int currentBonus) { return currentBonus >= 0 && currentBonus < maximumBonus; }
    public boolean matches(ItemStack stack) {
        return this == ARMOR ? CustomEnchantCapacityService.isArmor(stack) : CustomEnchantCapacityService.isWeapon(stack);
    }
    public static Optional<OrbType> fromStack(ItemStack stack) {
        if (stack.is(ModItems.ARMOR_ENCHANTMENT_ORB.get())) return Optional.of(ARMOR);
        if (stack.is(ModItems.WEAPON_ENCHANTMENT_ORB.get())) return Optional.of(WEAPON);
        return Optional.empty();
    }
}
