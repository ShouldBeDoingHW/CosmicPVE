package com.cosmicpve.equipment.enchantment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/** Selects one implemented same-tier enchantment, a valid level, and server-owned book rates. */
public final class UnexaminedBookOpeningService {
    public record Result(CosmicEnchantmentSpec enchantment, int level, BookOpeningRates rates) {}

    private final BookOpeningRateService rates;

    public UnexaminedBookOpeningService(BookOpeningRateService rates) {
        this.rates = rates;
    }

    public Optional<Result> roll(CosmicEnchantmentTier tier, List<CosmicEnchantmentSpec> available,
            RandomSource random, @Nullable ServerPlayer opener) {
        List<CosmicEnchantmentSpec> eligible = available.stream()
                .filter(spec -> spec.tier() == tier && spec.randomPoolEligible())
                .sorted(Comparator.comparing(spec -> spec.id().toString()))
                .toList();
        if (eligible.isEmpty()) return Optional.empty();
        CosmicEnchantmentSpec selected = eligible.get(random.nextInt(eligible.size()));
        int level = random.nextIntBetweenInclusive(1, selected.maxLevel());
        BookOpeningRates rolledRates = rates.roll(new BookOpeningRateContext(tier, opener), random);
        return Optional.of(new Result(selected, level, rolledRates));
    }
}
