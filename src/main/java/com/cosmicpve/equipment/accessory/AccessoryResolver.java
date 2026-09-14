package com.cosmicpve.equipment.accessory;

import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.registry.ModDataComponents;
import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Read-only effective-equipment boundary shared by accessory gameplay and presentation. */
public final class AccessoryResolver {
    public Optional<AmuletDefinition> equippedAmulet(LivingEntity entity) {
        var loadout = entity.getItemBySlot(EquipmentSlot.CHEST).get(ModDataComponents.ACCESSORY_LOADOUT.get());
        if (loadout == null || !loadout.valid()) return Optional.empty();
        return loadout.attached(AccessorySlot.AMULET).flatMap(AmuletDefinition::find);
    }
    public boolean hasAmulet(LivingEntity entity, AmuletDefinition definition) {
        return equippedAmulet(entity).filter(value -> value == definition).isPresent();
    }
}
