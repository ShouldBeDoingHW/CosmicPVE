package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards;
import net.minecraft.world.item.ItemStack;

public final class CosmicBookRateRules {
    private CosmicBookRateRules() {}
    public static boolean allows(ItemStack book, CosmicEnchantmentSpec spec, CosmicEnchantmentBookData data) {
        if (spec.tier().allowsRates(data.successRate(), data.destroyRate())) return true;
        var override = book.get(ModDataComponents.COSMIC_BOOK_RATE_OVERRIDE.get());
        return override != null && override.sourceId().equals(CosmicEnchantmentTableRewards.SOURCE_ID)
                && spec.id().equals(com.cosmicpve.registry.ModEnchantments.SOUL_SIPHON.identifier())
                && data.level() == spec.maxLevel()
                && CosmicEnchantmentTableRewards.MASTERY_SUCCESS.contains(data.successRate())
                && data.destroyRate() >= 51 && data.destroyRate() <= 100;
    }
}
