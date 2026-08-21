package com.cosmicpve.equipment.skin;

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
                .map(Component::copy)
                .map(line -> line.withStyle(ChatFormatting.YELLOW))
                .forEach(lines::add);
        lines.add(Component.translatable("tooltip.cosmicpve.skin.kind."
                + definition.weaponKind().name().toLowerCase(java.util.Locale.ROOT))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.cosmicpve.skin.attach").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.cosmicpve.skin.detach").withStyle(ChatFormatting.GRAY));
        return List.copyOf(lines);
    }

    public static Component active(WeaponSkinDefinition definition) {
        return Component.translatable("tooltip.cosmicpve.skin.active", definition.displayName())
                .withColor(definition.nameColor());
    }
}
