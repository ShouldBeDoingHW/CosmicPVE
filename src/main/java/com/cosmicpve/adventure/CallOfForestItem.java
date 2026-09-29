package com.cosmicpve.adventure;

import com.cosmicpve.data.component.CallOfForestData;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Server-authoritative Call of the Forest activation primitive. */
public final class CallOfForestItem extends Item {
    public CallOfForestItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.CALL_OF_FOREST.get());
        int minutes = data == null ? 0 : data.minutes();
        return Component.literal("Call of the Forest (" + minutes + ")")
                .withStyle(s -> s.withColor(color(minutes)).withBold(true).withItalic(true).withUnderlined(true));
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        var data = stack.get(ModDataComponents.CALL_OF_FOREST.get());
        if (data != null) tooltip.accept(Component.literal("Adventure duration: " + data.minutes() + " minutes")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("Right-click to enter the Dense Woodlands.").withStyle(ChatFormatting.GRAY));
    }
    @Override public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        ItemStack stack = player.getItemInHand(hand);
        var data = stack.get(ModDataComponents.CALL_OF_FOREST.get());
        if (data == null || data.version() != CallOfForestData.CURRENT_DATA_VERSION) return InteractionResult.FAIL;
        var entry = DenseWoodlandsBootstrap.SESSIONS.tryEnter(serverPlayer, data.minutes(), hand);
        if (!entry.accepted()) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
    }
    private static int color(int minutes) { return minutes == 10 ? 0x43B03C : minutes == 20 ? 0x23731E : 0x0E3D0B; }
}
