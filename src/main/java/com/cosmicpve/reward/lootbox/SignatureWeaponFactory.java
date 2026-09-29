package com.cosmicpve.reward.lootbox;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class SignatureWeaponFactory {
    public ItemStack create(SignatureWeaponDefinition definition, RegistryAccess access) {
        ItemStack stack = new ItemStack(definition.baseItem());
        var set = CosmicContent.repository().findArmorSetDefinition(definition.matchingSetId()).orElseThrow(() ->
                new IllegalStateException("Missing armor set for signature weapon: " + definition.matchingSetId()));
        stack.set(ModDataComponents.SIGNATURE_WEAPON.get(), new SignatureWeaponIdentity(
                SignatureWeaponIdentity.CURRENT_DATA_VERSION, definition.id(), definition.matchingSetId(), definition.kind()));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(definition.displayName()).withColor(set.presentationColor()));
        var registry = access.lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            if (definition.kind() == SignatureWeaponIdentity.Kind.MELEE) {
                mutable.set(registry.getOrThrow(Enchantments.SHARPNESS), 5);
            } else if (definition == SignatureWeaponDefinition.RANGERS_BOW) {
                mutable.set(registry.getOrThrow(Enchantments.POWER), 5);
                mutable.set(registry.getOrThrow(Enchantments.INFINITY), 1);
                mutable.set(registry.getOrThrow(Enchantments.FLAME), 1);
            } else {
                mutable.set(registry.getOrThrow(Enchantments.QUICK_CHARGE), 3);
                mutable.set(registry.getOrThrow(Enchantments.PIERCING), 4);
            }
            mutable.set(registry.getOrThrow(Enchantments.UNBREAKING), 3);
            if (definition != SignatureWeaponDefinition.RANGERS_BOW) {
                mutable.set(registry.getOrThrow(Enchantments.MENDING), 1);
            }
        });
        return stack;
    }
}
