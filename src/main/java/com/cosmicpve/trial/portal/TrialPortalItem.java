package com.cosmicpve.trial.portal;

import com.cosmicpve.trial.TrialRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Consumer;

public final class TrialPortalItem extends Item {
    public TrialPortalItem(Properties properties) { super(properties); }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
        var result = TrialRuntime.sessions().createPortal(player, context.getClickedPos().relative(context.getClickedFace()));
        if (!result.success()) {
            player.displayClientMessage(Component.literal(result.message()), true);
            return InteractionResult.FAIL;
        }
        if (!player.isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.cosmicpve.trial_portal.description")
                .withStyle(style -> style.withColor(0xFFFF55)));
        tooltip.accept(Component.translatable("item.cosmicpve.trial_portal.use")
                .withStyle(style -> style.withColor(0xAAAAAA)));
    }
}
