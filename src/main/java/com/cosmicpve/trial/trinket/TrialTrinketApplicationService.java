package com.cosmicpve.trial.trinket;

import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class TrialTrinketApplicationService {
    public enum Outcome { SUCCESS, REJECTED_INVALID, REJECTED_PORTAL_STACK, REJECTED_EQUAL_OR_WEAKER, REJECTED_STALE }

    public Outcome apply(ItemStack carried, ItemStack target, ItemStack authoritativeTarget) {
        if (target != authoritativeTarget) return Outcome.REJECTED_STALE;
        if (!target.is(ModItems.TRIAL_PORTAL.get())) return Outcome.REJECTED_INVALID;
        if (target.getCount() != 1) return Outcome.REJECTED_PORTAL_STACK;
        var trinket = carried.get(ModDataComponents.TRIAL_TRINKET.get());
        if (trinket == null || !trinket.valid()) return Outcome.REJECTED_INVALID;
        var modifiers = target.getOrDefault(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), TrialPortalModifiers.EMPTY);
        if (!modifiers.valid()) return Outcome.REJECTED_INVALID;
        if (trinket.value() <= modifiers.value(trinket.type())) return Outcome.REJECTED_EQUAL_OR_WEAKER;
        com.cosmicpve.trial.portal.TrialPortalItem.applyModifiers(target, modifiers.with(trinket));
        carried.shrink(1);
        return Outcome.SUCCESS;
    }
}
