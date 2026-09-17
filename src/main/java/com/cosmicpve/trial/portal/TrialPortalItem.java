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
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;

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
        var modifiers = stack.getOrDefault(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), TrialPortalModifiers.EMPTY);
        tooltipLines(modifiers).forEach(tooltip);
    }

    public static List<Component> tooltipLines(TrialPortalModifiers modifiers) {
        var lines = new ArrayList<Component>(11);
        lines.add(Component.translatable("item.cosmicpve.trial_portal.description").withColor(0xFFFF55));
        lines.add(Component.translatable("item.cosmicpve.trial_portal.use").withColor(0xAAAAAA));
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.cosmicpve.trial_portal.trinkets")
                .withColor(0xFFAA00).withStyle(ChatFormatting.BOLD));
        lines.addAll(modifierLines(modifiers));
        return List.copyOf(lines);
    }

    public static List<Component> modifierLines(TrialPortalModifiers modifiers) {
        var lines = new ArrayList<Component>(6);
        if (modifiers.skipRooms() > 0) addModifier(lines,
                Component.translatable(modifiers.skipRooms() == 1
                                ? "tooltip.cosmicpve.trial_portal.skip_one"
                                : "tooltip.cosmicpve.trial_portal.skip", modifiers.skipRooms()),
                com.cosmicpve.data.component.TrialTrinketType.SKIP.presentationColor(),
                Component.translatable(modifiers.skipRooms() == 1
                        ? "tooltip.cosmicpve.trial_portal.skip_description_one"
                        : "tooltip.cosmicpve.trial_portal.skip_description", modifiers.skipRooms()));
        if (modifiers.timeBonusSeconds() % 60 != 0) addModifier(lines,
                Component.literal("Extra Time: +" + modifiers.timeBonusSeconds() + " Seconds"),
                com.cosmicpve.data.component.TrialTrinketType.TIME.presentationColor(),
                Component.literal("Added to the starting Trial timer."));
        else if (modifiers.timeMinutes() > 0) addModifier(lines,
                Component.translatable(modifiers.timeMinutes() == 1
                                ? "tooltip.cosmicpve.trial_portal.time_one"
                                : "tooltip.cosmicpve.trial_portal.time", modifiers.timeMinutes()),
                com.cosmicpve.data.component.TrialTrinketType.TIME.presentationColor(),
                Component.translatable(modifiers.timeMinutes() == 1
                        ? "tooltip.cosmicpve.trial_portal.time_description_one"
                        : "tooltip.cosmicpve.trial_portal.time_description", modifiers.timeMinutes()));
        if (modifiers.insuranceLevel() > 0) addModifier(lines,
                Component.translatable(modifiers.insuranceLevel() == 1
                                ? "tooltip.cosmicpve.trial_portal.insurance_one"
                                : "tooltip.cosmicpve.trial_portal.insurance", modifiers.insuranceLevel()),
                com.cosmicpve.data.component.TrialTrinketType.INSURANCE.presentationColor(),
                Component.translatable(modifiers.insuranceLevel() == 1
                        ? "tooltip.cosmicpve.trial_portal.insurance_description_one"
                        : "tooltip.cosmicpve.trial_portal.insurance_description", modifiers.insuranceLevel()));
        if (modifiers.famePercent() > 0) addModifier(lines,
                Component.translatable("tooltip.cosmicpve.trial_portal.fame", modifiers.famePercent()),
                com.cosmicpve.data.component.TrialTrinketType.FAME.presentationColor(),
                Component.translatable("tooltip.cosmicpve.trial_portal.fame_description", modifiers.famePercent()));
        if (modifiers.madnessOptionBonus() > 0) addModifier(lines,
                Component.literal("Bonus Madness Ballot: +" + modifiers.madnessOptionBonus() + " Options"),
                0x8C1708, Component.literal("Up to " + modifiers.madnessChoices(8) + " distinct choices per vote."));
        if (lines.isEmpty()) lines.add(Component.translatable("tooltip.cosmicpve.trial_portal.none").withColor(0x777777));
        return List.copyOf(lines);
    }

    private static void addModifier(List<Component> lines, Component name, int color, Component description) {
        lines.add(Component.literal("✖ ").withColor(0xFFAA00)
                .append(name.copy().withStyle(style -> style.withColor(color).withBold(true))));
        lines.add(Component.literal("  ").append(description.copy().withColor(0x777777)));
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
