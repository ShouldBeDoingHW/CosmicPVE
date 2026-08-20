package com.cosmicpve.equipment.skin;

import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;

public final class WeaponSkinResolver {
    public Optional<WeaponSkinDefinition> resolve(ItemStack stack) {
        var identity = stack.get(ModDataComponents.WEAPON_SKIN.get());
        return identity == null ? Optional.empty() : WeaponSkinDefinitions.find(identity.skinId());
    }

    public List<VirtualEnchantmentGrant> virtualEnchantments(ItemStack stack) {
        return resolve(stack).map(WeaponSkinDefinition::virtualEnchantments).orElse(List.of());
    }
}
