package com.cosmicpve.reward.preview;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Enumerates production outcomes without consuming a source or using the player's reward RNG.
 * Book identity is enchantment + level + Success option: random Destroy rolls are
 * represented as a range on one entry, not duplicated into separate outcomes.
 */
public interface LootPreviewProvider {
    List<ItemStack> previewOutcomes(ServerPlayer player, ItemStack source);

    default Component previewTitle(ItemStack source) {
        return source.getHoverName().copy();
    }
}
