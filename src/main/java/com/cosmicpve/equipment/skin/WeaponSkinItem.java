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
        if (definition.isEmpty()) return super.getName(stack);
        var skin = definition.orElseThrow();
        return Component.literal("Item Skin (").withColor(0xFFFFFF)
                .append(skin.displayName().copy().withColor(skin.nameColor()))
                .append(Component.literal(")").withColor(0xFFFFFF));
    }
}
