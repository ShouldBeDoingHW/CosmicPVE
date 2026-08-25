package com.cosmicpve.equipment.mask;

import com.cosmicpve.data.component.MaskPresentation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public final class MaskLore {
    private MaskLore() {}
    public static List<Component> lines(List<MaskPresentation> presentations, boolean applicationItem) {
        var lines = new ArrayList<Component>();
        if (presentations.size() > 1)
            lines.add(Component.translatable("tooltip.cosmicpve.mask.contains").withStyle(ChatFormatting.GRAY));
        for (var presentation : presentations) {
            if (presentations.size() > 1) lines.add(Component.literal("• ").withColor(presentation.color())
                    .append(presentation.displayName().copy().withColor(presentation.color())));
            lines.add((presentations.size() > 1 ? Component.literal("  ") : Component.empty())
                    .append(presentation.effectSummary().copy().withStyle(ChatFormatting.YELLOW)));
        }
        if (applicationItem) {
            lines.add(Component.translatable("tooltip.cosmicpve.mask.applies_to").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.cosmicpve.mask.apply").withStyle(ChatFormatting.GRAY));
        } else lines.add(Component.translatable("tooltip.cosmicpve.mask.remove").withStyle(ChatFormatting.GRAY));
        return List.copyOf(lines);
    }

    /** Compact helmet metadata: one identity line plus the established removal instruction. */
    public static List<Component> attached(List<MaskPresentation> presentations) {
        if (presentations.isEmpty()) return List.of();
        return List.of(attachedIdentity(presentations),
                Component.translatable("tooltip.cosmicpve.mask.remove").withStyle(ChatFormatting.GRAY));
    }

    /** One indivisible logical row; the client gather hook reserves its complete rendered width. */
    public static Component attachedIdentity(List<MaskPresentation> presentations) {
        if (presentations.isEmpty()) return Component.empty();
        var identity = Component.translatable("tooltip.cosmicpve.mask.attached_prefix")
                .withStyle(ChatFormatting.WHITE);
        if (presentations.size() == 1) {
            identity.append(presentations.getFirst().displayName().copy()
                    .withColor(presentations.getFirst().color()));
        } else {
            identity.append(Component.translatable("tooltip.cosmicpve.mask.multi_prefix")
                    .withStyle(ChatFormatting.WHITE));
            for (int i = 0; i < presentations.size(); i++) {
                if (i > 0) identity.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
                var presentation = presentations.get(i);
                identity.append(presentation.displayName().copy().withColor(presentation.color()));
            }
            identity.append(Component.literal(")").withStyle(ChatFormatting.WHITE));
        }
        return identity;
    }

    /** Client-rendered as one measured row so only this identity bypasses ordinary text wrapping. */
    public record AttachedIdentityTooltip(Component text) implements TooltipComponent {}
}
