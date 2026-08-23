package com.cosmicpve.equipment.enchantment;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.util.RandomSource;

/** Owns base rarity ranges and ordered modifier composition for Unexamined Book rate rolls. */
public final class BookOpeningRateService {
    private final CopyOnWriteArrayList<BookOpeningRateModifier> modifiers;

    public BookOpeningRateService() {
        this(List.of());
    }

    public BookOpeningRateService(List<BookOpeningRateModifier> modifiers) {
        this.modifiers = new CopyOnWriteArrayList<>(modifiers);
    }

    public void register(BookOpeningRateModifier modifier) {
        modifiers.add(Objects.requireNonNull(modifier));
    }

    public BookOpeningRates roll(BookOpeningRateContext context, RandomSource random) {
        return applyModifiers(context, new BookOpeningRates(
                context.tier().randomSuccess(random), context.tier().randomDestroy(random)));
    }

    public BookOpeningRates applyModifiers(BookOpeningRateContext context, BookOpeningRates base) {
        BookOpeningRates current = base;
        for (var modifier : modifiers) current = Objects.requireNonNull(modifier.modify(context, current));
        if (!context.tier().allowsRates(current.successRate(), current.destroyRate())) {
            throw new IllegalStateException("Book-opening modifier produced rates invalid for " + context.tier());
        }
        return current;
    }
}
