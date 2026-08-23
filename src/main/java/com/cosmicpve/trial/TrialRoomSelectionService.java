package com.cosmicpve.trial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public final class TrialRoomSelectionService {
    private final List<TrialRoomWeightModifier> modifiers = new ArrayList<>();
    public void register(TrialRoomWeightModifier modifier) { modifiers.add(modifier); }
    public int weight(TrialSession session, Identifier room) {
        int result = 5 - 2 * session.progress().appearances(room);
        for (var modifier : modifiers) result += modifier.modify(session, room);
        return result;
    }
    public Optional<Identifier> select(TrialSession session, List<Identifier> pool, RandomSource random) {
        record Weighted(Identifier id, int weight) {}
        var eligible = pool.stream().filter(id -> !session.progress().lastRoom().filter(id::equals).isPresent())
                .map(id -> new Weighted(id, weight(session, id))).filter(entry -> entry.weight() > 0).toList();
        int total = eligible.stream().mapToInt(Weighted::weight).sum();
        if (total <= 0) return Optional.empty();
        int target = random.nextInt(total);
        for (var entry : eligible) { target -= entry.weight(); if (target < 0) return Optional.of(entry.id()); }
        throw new IllegalStateException("Positive Trial room weights produced no selection");
    }
}
