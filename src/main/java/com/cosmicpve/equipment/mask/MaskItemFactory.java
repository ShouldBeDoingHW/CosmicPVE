package com.cosmicpve.equipment.mask;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.data.component.MaskPresentation;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class MaskItemFactory {
    private MaskItemFactory() {}
    public static ItemStack create(Identifier id) { return create(List.of(id)); }
    public static ItemStack create(List<Identifier> ids) { return create(ids, CosmicContent.repository()); }
    public static ItemStack create(List<Identifier> ids, com.cosmicpve.content.CosmicContentRepository content) {
        var definitions = ids.stream().map(content::requireMaskDefinition).toList();
        String texture = ids.size() == 1
                ? content.requireMaskDefinition(ids.getFirst()).profileTexture()
                : MaskProfiles.MULTI_TEXTURE;
        var presentations = definitions.stream().map(definition -> new MaskPresentation(definition.id(),
                definition.displayName(), definition.effectSummary(), definition.presentationColor())).toList();
        var loadout = new MaskLoadout(ids, texture, presentations);
        if (!loadout.valid()) throw new IllegalArgumentException("Mask item requires 1-5 distinct masks");
        var stack = new ItemStack(ModItems.MASK.get());
        stack.set(ModDataComponents.MASK_ITEM.get(), loadout);
        stack.set(DataComponents.PROFILE, MaskProfiles.profile(texture));
        return stack;
    }
}
