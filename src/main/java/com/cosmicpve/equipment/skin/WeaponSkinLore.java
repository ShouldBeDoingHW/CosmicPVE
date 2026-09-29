package com.cosmicpve.equipment.skin;

import com.cosmicpve.equipment.CosmeticEffectStyle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Compact, metadata-driven player presentation for skin items and attached skins. */
public final class WeaponSkinLore {
    private WeaponSkinLore() {}

    public static List<Component> applicationItem(WeaponSkinDefinition definition) {
        var lines = new ArrayList<Component>();
        definition.effectDescription().stream()
                .map(CosmeticEffectStyle::apply)
                .forEach(lines::add);
        lines.add(Component.empty());
        lines.add(Component.literal("Attach this skin to any ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                .append(Component.literal(definition.weaponKind().name()).withStyle(style -> style
                        .withColor(ChatFormatting.WHITE).withItalic(true).withUnderlined(true))));
        lines.add(Component.literal("to over-ride its visual appearance.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        lines.add(Component.empty());
        lines.add(Component.literal("Drag n' Drop onto item to attach.").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Right-Click item to detach skin.").withStyle(ChatFormatting.GRAY));
        return List.copyOf(lines);
    }

    public static Component active(WeaponSkinDefinition definition) {
        return Component.translatable("tooltip.cosmicpve.skin.active", definition.displayName())
                .withStyle(style -> style.withColor(definition.nameColor()).withBold(true));
    }
}
