package com.cosmicpve.adventure.ranger;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WrittenBookContent;

/** Canonical nine-scrap summon item. Item identity, not display text, authorizes the encounter. */
public final class RangerTomb {
    public static final int GREEN = 0x43B03C;
    public static final String NAME = "Desecrated Tomb: Ranger";
    private RangerTomb() {}

    public static Item.Properties applyDefaults(Item.Properties properties) {
        var title = Filterable.passThrough(NAME);
        var page = Component.literal("THE RANGER\n\n").withStyle(s -> s.withColor(GREEN).withBold(true))
                .append(Component.literal("Place this desecrated tomb upon the lectern within a Woodlands Arena to call forth the Cosmic Ranger.")
                        .withStyle(s -> s.withColor(0x23731E).withBold(false)));
        return properties
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .component(DataComponents.CUSTOM_NAME, Component.literal(NAME)
                        .withStyle(s -> s.withColor(GREEN).withBold(true).withItalic(false)))
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("Nine scraps bound into something that should have remained buried.")
                                .withStyle(s -> s.withColor(0x0E3D0B).withItalic(true)),
                        Component.empty(),
                        Component.literal("Place inside the lectern of a Woodlands Arena")
                                .withStyle(s -> s.withColor(GREEN).withItalic(false)),
                        Component.literal("to summon the Cosmic Ranger.")
                                .withStyle(s -> s.withColor(GREEN).withItalic(false)))))
                .component(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                        title, "Unknown", 0, List.of(Filterable.passThrough(page)), true));
    }

    public static boolean isCanonical(ItemStack stack) {
        return stack.is(com.cosmicpve.registry.ModItems.DESECRATED_TOMB_RANGER.get())
                && stack.has(DataComponents.WRITTEN_BOOK_CONTENT);
    }
}
