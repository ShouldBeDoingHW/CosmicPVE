package com.cosmicpve.client;

import com.cosmicpve.equipment.armor.ArmorSetPresentationResolver;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

final class ArmorSetClientExtensions {
    private ArmorSetClientExtensions() {}

    static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions tint = new IClientItemExtensions() {
            @Override public int getArmorLayerTintColor(net.minecraft.world.item.ItemStack stack,
                    EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
                var color = ArmorSetPresentationResolver.color(stack);
                return color.isEmpty()
                        ? IClientItemExtensions.super.getArmorLayerTintColor(stack, layer, layerIdx, fallbackColor)
                        : 0xFF000000 | color.getAsInt();
            }
        };
        var items = BuiltInRegistries.ITEM.stream()
                .filter(item -> {
                    var equippable = item.getDefaultInstance().get(DataComponents.EQUIPPABLE);
                    return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                            && !event.isItemRegistered(item);
                }).toArray(net.minecraft.world.item.Item[]::new);
        event.registerItem(tint, items);
    }
}
