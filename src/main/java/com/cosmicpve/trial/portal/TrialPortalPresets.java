package com.cosmicpve.trial.portal;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class TrialPortalPresets {
    private TrialPortalPresets() {}
    public static ItemStack generate(Identifier id) {
        var modifiers = CosmicContent.repository().snapshot().trialPortalPresets().get(id);
        if (modifiers == null) throw new IllegalArgumentException("Unknown Trial Portal preset: " + id);
        ItemStack stack = new ItemStack(ModItems.TRIAL_PORTAL.get());
        if (!modifiers.isEmpty()) TrialPortalItem.applyModifiers(stack, modifiers);
        return stack;
    }
}
