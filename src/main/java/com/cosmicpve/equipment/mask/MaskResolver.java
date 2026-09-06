package com.cosmicpve.equipment.mask;

import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.mask.MaskDefinition;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class MaskResolver {
    private final CosmicContentRepository content;
    public MaskResolver(CosmicContentRepository content) { this.content = content; }
    public List<MaskDefinition> resolve(ItemStack stack) {
        MaskLoadout loadout = stack.get(ModDataComponents.MASK_LOADOUT.get());
        if (loadout == null || !loadout.valid()) return List.of();
        var result = new ArrayList<MaskDefinition>();
        for (var id : loadout.maskIds()) {
            var definition = content.findMaskDefinition(id);
            if (definition.isEmpty()) return List.of();
            result.add(definition.orElseThrow());
        }
        return List.copyOf(result);
    }
    public List<MaskDefinition> resolve(LivingEntity entity) { return com.cosmicpve.adventure.AdventureRules.restricted(entity) ? List.of() : resolve(entity.getItemBySlot(EquipmentSlot.HEAD)); }
}
