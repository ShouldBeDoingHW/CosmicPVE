package com.cosmicpve.reward.spawner;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.RewardDeliveryService;
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
        new RewardDeliveryService().deliver(serverPlayer, java.util.List.of(reward));
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                OPEN_SOUND_SOURCE, 1.0F, 1.0F);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(held);
    }
}
