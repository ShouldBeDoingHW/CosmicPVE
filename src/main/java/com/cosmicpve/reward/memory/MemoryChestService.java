package com.cosmicpve.reward.memory;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.animation.LootAnimationPreviewProvider;
import com.cosmicpve.reward.animation.SingleRewardAnimationService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class MemoryChestService {
    public static final MemoryChestService INSTANCE = new MemoryChestService();
    private MemoryChestService() {}

    public boolean open(ServerPlayer player, ItemStack source) {
        if (!source.is(ModItems.MEMORY_CHEST.get())
                || SingleRewardAnimationService.INSTANCE.active(player)
                || player.getData(ModAttachments.LOOT_ANIMATION).valid()) return false;
        ItemStack finalReward = MemoryChestRewards.select(player.getRandom());
        var previews = LootAnimationPreviewProvider.weighted(MemoryChestRewards.ENTRIES.stream()
                .map(entry -> new LootAnimationPreviewProvider.WeightedPreview(entry.create(), entry.weight())).toList());
        return SingleRewardAnimationService.INSTANCE.open(player, finalReward, previews, () -> source.shrink(1));
    }
}
