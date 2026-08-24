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
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;

public final class TrialPortalItem extends Item {
    public TrialPortalItem(Properties properties) { super(properties); }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
        var modifiers = context.getItemInHand().getOrDefault(
                ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), TrialPortalModifiers.EMPTY);
        if (!modifiers.valid() || (!modifiers.isEmpty() && context.getItemInHand().getCount() != 1)) {
            player.displayClientMessage(Component.literal("That Trial Portal contains invalid modifier data."), true);
            return InteractionResult.FAIL;
        }
        var result = TrialRuntime.sessions().createPortal(player,
                context.getClickedPos().relative(context.getClickedFace()), modifiers);
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
        var modifiers = stack.getOrDefault(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), TrialPortalModifiers.EMPTY);
        modifierLines(modifiers).forEach(tooltip);
    }

    public static java.util.List<Component> modifierLines(TrialPortalModifiers modifiers) {
        var lines = new java.util.ArrayList<Component>(3);
        if (modifiers.timeMinutes() > 0) lines.add(Component.translatable(
                modifiers.timeMinutes() == 1 ? "tooltip.cosmicpve.trial_portal.time_one"
                        : "tooltip.cosmicpve.trial_portal.time", modifiers.timeMinutes()).withColor(
                                com.cosmicpve.data.component.TrialTrinketType.TIME.presentationColor()));
        if (modifiers.skipRooms() > 0) lines.add(Component.translatable(
                modifiers.skipRooms() == 1 ? "tooltip.cosmicpve.trial_portal.skip_one"
                        : "tooltip.cosmicpve.trial_portal.skip", modifiers.skipRooms()).withColor(
                                com.cosmicpve.data.component.TrialTrinketType.SKIP.presentationColor()));
        if (modifiers.insuranceLevel() > 0) lines.add(Component.translatable(
                "tooltip.cosmicpve.trial_portal.insurance", modifiers.insuranceLevel()).withColor(
                        com.cosmicpve.data.component.TrialTrinketType.INSURANCE.presentationColor()));
        return java.util.List.copyOf(lines);
    }

    /** Applies the persistent modifier and its per-stack maximum as one invariant. */
    public static void applyModifiers(ItemStack stack, TrialPortalModifiers modifiers) {
        if (!stack.is(com.cosmicpve.registry.ModItems.TRIAL_PORTAL.get()) || stack.getCount() != 1
                || modifiers == null || !modifiers.valid() || modifiers.isEmpty()) {
            throw new IllegalArgumentException("Modified Trial Portals must be valid single-item stacks");
        }
        stack.set(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), modifiers);
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
    }
}
