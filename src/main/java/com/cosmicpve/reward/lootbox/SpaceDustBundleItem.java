package com.cosmicpve.reward.lootbox;

import com.cosmicpve.reward.animation.LootAnimationPreviewProvider;
import com.cosmicpve.reward.animation.SingleRewardAnimationService;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SpaceDustBundleItem extends Item {
    public SpaceDustBundleItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) { return Component.literal("Space Dust Bundle")
            .withStyle(style -> style.withColor(0x8F5B11).withBold(true).withUnderlined(true)); }
    public static List<Component> lore() { return List.of(Component.literal(
            "100 million years of deep space activity has led to this moment.")
            .withStyle(style -> style.withColor(0x8F5B11).withItalic(true))); }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack source = player.getItemInHand(hand);
        var rewards = SpaceDustBundleRewards.roll(player.getRandom());
        var previews = SpaceDustBundleRewards.WEIGHTS.stream().map(entry ->
                com.cosmicpve.equipment.enchantment.CosmicDustService.dust(entry.tier(), 1)).toList();
        return SingleRewardAnimationService.INSTANCE.open(serverPlayer, rewards,
                LootAnimationPreviewProvider.uniform(previews), () -> source.shrink(1))
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }
}
