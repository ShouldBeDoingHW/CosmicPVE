package com.cosmicpve.equipment.skin;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;

public record WeaponSkinDefinition(
        Identifier id, Component displayName, int nameColor, List<Component> effectDescription,
        WeaponKind weaponKind, Identifier itemModel,
        List<VirtualEnchantmentGrant> virtualEnchantments) {
    public WeaponSkinDefinition {
        effectDescription = List.copyOf(effectDescription);
        virtualEnchantments = List.copyOf(virtualEnchantments);
    }

    public boolean accepts(ItemStack stack) {
        return weaponKind == WeaponKind.AXE
                ? stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem
                : stack.is(ItemTags.SWORDS)
                        || BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_sword");
    }

    public enum WeaponKind { AXE, SWORD }
}
