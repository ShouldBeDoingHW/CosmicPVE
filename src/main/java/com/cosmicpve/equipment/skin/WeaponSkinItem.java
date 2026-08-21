package com.cosmicpve.equipment.skin;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WeaponSkinItem extends Item {
    public WeaponSkinItem(Properties properties) { super(properties); }

    @Override
    public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.WEAPON_SKIN_ITEM.get());
        if (data == null) return super.getName(stack);
        var definition = WeaponSkinDefinitions.find(data.skinId());
        return definition.isPresent()
                ? Component.translatable("item.cosmicpve.weapon_skin.named", definition.orElseThrow().displayName())
                        .withColor(definition.orElseThrow().nameColor())
                : super.getName(stack);
    }
}
