package com.cosmicpve.vkit;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.animation.LootAnimationFeedback;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class GodlyVKitBundleItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final String LORE = "Found nested inside the deepest corner of the earth, there is unimaginable power within this bag. Right click to open!";
    private final RewardDeliveryService delivery = new RewardDeliveryService();
    public GodlyVKitBundleItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public Component getName(ItemStack stack) {
        return segment("Godly ", 0xF7658D).append(segment("Vkit ", 0xF22960)).append(segment("Bundle!", 0xFA0246));
    }
    private static net.minecraft.network.chat.MutableComponent segment(String text, int color) {
        return Component.literal(text).withStyle(s -> s.withColor(color).withBold(true));
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.literal(LORE).withStyle(style -> style.withColor(0xFFFF55)));
    }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack held = player.getItemInHand(hand);
        List<ItemStack> rewards = List.of(new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()),
                new ItemStack(ModItems.OGRE_VKIT_CRYSTAL.get()), new ItemStack(ModItems.JUDGEMENT_VKIT_CRYSTAL.get()),
                new ItemStack(ModItems.SLAYER_VKIT_CRYSTAL.get()));
        return commit(held, rewards, primary -> player.setItemInHand(hand, primary.copy()),
                extras -> delivery.deliver(serverPlayer, extras), () -> LootAnimationFeedback.bundle(serverPlayer));
    }
    static InteractionResult.Success commit(ItemStack bundle, List<ItemStack> rewards,
            Consumer<ItemStack> placePrimary, Consumer<List<ItemStack>> deliverExtras, Runnable feedback) {
        if (bundle.isEmpty() || rewards.size() != 4 || rewards.stream().anyMatch(ItemStack::isEmpty))
            throw new IllegalArgumentException("Godly bundle requires exactly four valid rewards");
        bundle.shrink(1);
        // Occupy the just-freed authoritative hand slot before safe-delivering the other three.
        // Otherwise the first extra can enter that slot and then be overwritten by Minecraft's
        // successful-use hand transformation.
        placePrimary.accept(rewards.getFirst().copy());
        deliverExtras.accept(rewards.subList(1, rewards.size()).stream().map(ItemStack::copy).toList());
        feedback.run();
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(rewards.getFirst().copy());
    }
}
