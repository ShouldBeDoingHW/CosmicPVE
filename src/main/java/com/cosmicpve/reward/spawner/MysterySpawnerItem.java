package com.cosmicpve.reward.spawner;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class MysterySpawnerItem extends Item {
    public static final SoundSource OPEN_SOUND_SOURCE = SoundSource.MASTER;
    public MysterySpawnerItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.MYSTERY_SPAWNER.get());
        return data == null ? super.getName(stack) : Component.translatable(
                "item.cosmicpve.mystery_spawner." + data.tier().getSerializedName())
                .withStyle(style -> style.withColor(data.tier().color()).withBold(true));
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("tooltip.cosmicpve.mystery_spawner.open").withStyle(ChatFormatting.GRAY));
    }

    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack held = player.getItemInHand(hand);
        ItemStack reward = MysterySpawners.openAndConsume(held, serverPlayer.getRandom()::nextInt);
        if (reward.isEmpty()) return InteractionResult.FAIL;
        InteractionResult.Success result = commitOpening(held, reward,
                generated -> new RewardDeliveryService().deliver(serverPlayer, List.of(generated)));
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                OPEN_SOUND_SOURCE, 1.0F, 1.0F);
        return result;
    }

    static InteractionResult.Success commitOpening(ItemStack remainingSource, ItemStack reward,
            Consumer<ItemStack> rewardDelivery) {
        if (remainingSource.isEmpty()) {
            // ItemStack.use writes this transformed result back to the used hand after use()
            // returns. The final source unit must therefore transform directly into the
            // generated spawner instead of delivering into a slot that writeback can erase.
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(reward);
        }
        rewardDelivery.accept(reward);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(remainingSource);
    }
}
