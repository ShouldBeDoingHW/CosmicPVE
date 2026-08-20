package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;

public final class TransmogApplicationService {
    public enum Outcome { SUCCESS, REJECTED_INVALID_SCROLL, REJECTED_TARGET, REJECTED_ALREADY_APPLIED, STALE_TARGET }

    public Outcome apply(ItemStack scroll, ItemStack expected, ItemStack target) {
        if (expected != target) return Outcome.STALE_TARGET;
        if (!scroll.is(ModItems.TRANSMOG_SCROLL.get())) return Outcome.REJECTED_INVALID_SCROLL;
        if (!EquipmentInteractionPolicy.isPotentialEquipment(target)) return Outcome.REJECTED_TARGET;
        var metadata = target.getOrDefault(ModDataComponents.CUSTOM_ENCHANT_META.get(), CustomEnchantMetadata.DEFAULT);
        if (metadata.transmogSorted()) return Outcome.REJECTED_ALREADY_APPLIED;
        target.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), metadata.withTransmogSorted(true));
        scroll.shrink(1);
        return Outcome.SUCCESS;
    }

    public boolean isApplied(ItemStack stack) {
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        return metadata != null && metadata.transmogSorted();
    }
}
