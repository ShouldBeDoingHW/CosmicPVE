package com.cosmicpve.equipment.armor;

import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shared visual identity for genuine Armor Set and Omni armor; it has no gameplay-resolution role. */
public final class ArmorSetPresentationResolver {
    public static final int OMNI_COLOR = 0x061630;
    private ArmorSetPresentationResolver() {}

    public static Optional<Presentation> resolve(ItemStack stack) {
        var identity = stack.get(ModDataComponents.ARMOR_SET_ID.get());
        if (identity != null) return Optional.of(new Presentation(
                identity.displayName(), identity.color(), identity.fullSetBonus(), false));
        if (Boolean.TRUE.equals(stack.get(ModDataComponents.OMNI_ARMOR.get())))
            return Optional.of(new Presentation(Component.literal("Omni"), OMNI_COLOR, List.of(), true));
        return Optional.empty();
    }

    public static OptionalInt color(ItemStack stack) {
        return resolve(stack).map(value -> OptionalInt.of(value.color())).orElseGet(OptionalInt::empty);
    }

    public record Presentation(Component displayName, int color, List<Component> fullSetBonus, boolean omni) {
        public Presentation { fullSetBonus = List.copyOf(fullSetBonus); }
    }
}
