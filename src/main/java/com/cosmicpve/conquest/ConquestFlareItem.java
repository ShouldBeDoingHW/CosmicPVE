package com.cosmicpve.conquest;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class ConquestFlareItem extends Item {
    public static final int COLOR = 0xBF0000;
    public ConquestFlareItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) { return super.getName(stack).copy().withColor(COLOR); }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        return useServer(serverPlayer, hand, serverPlayer::sendSystemMessage);
    }

    InteractionResult useServer(ServerPlayer serverPlayer, InteractionHand hand, Consumer<Component> feedback) {
        ItemStack stack = serverPlayer.getItemInHand(hand);
        var result = ConquestRuntime.events().spawnNear(serverPlayer.level(), ConquestOrigin.FLARE,
                serverPlayer.blockPosition(), ConquestEventService.FLARE_RADIUS, serverPlayer.getRandom(), true);
        return finishUse(serverPlayer, stack, result, feedback);
    }

    static InteractionResult finishUse(ServerPlayer player, ItemStack stack, Optional<ConquestEvent> result,
            Consumer<Component> feedback) {
        if (result.isEmpty()) {
            feedback.accept(Component.literal("No valid Conquest Chest location was found nearby."));
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        feedback.accept(Component.literal("A Conquest Chest answered the Flare at "
                + result.orElseThrow().chestPosition().toShortString() + "."));
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.cosmicpve.conquest_flare.description")
                .withStyle(style -> style.withColor(COLOR)));
        tooltip.accept(Component.translatable("tooltip.cosmicpve.conquest_flare.use")
                .withStyle(ChatFormatting.GRAY));
    }
}
