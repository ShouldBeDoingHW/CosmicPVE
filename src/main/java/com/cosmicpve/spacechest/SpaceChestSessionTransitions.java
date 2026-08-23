package com.cosmicpve.spacechest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Pure transaction helpers shared by runtime code and persistence-focused tests. */
public final class SpaceChestSessionTransitions {
    private SpaceChestSessionTransitions() {}

    public static Optional<SpaceChestSession> select(SpaceChestSession current, int slot) {
        if (current.phase() != SpaceChestPhase.SELECTING || slot < 0 || slot >= 27
                || current.selectedSlots().contains(slot) || current.selectedSlots().size() >= 5) return Optional.empty();
        var selected = new ArrayList<>(current.selectedSlots());
        selected.add(slot);
        return Optional.of(new SpaceChestSession(current.dataVersion(), current.tier(), current.phase(), selected, List.of()));
    }

    public static SpaceChestSession commit(SpaceChestSession selected, List<List<ItemStack>> generated) {
        if (selected.phase() != SpaceChestPhase.SELECTING || selected.selectedSlots().size() != 5 || generated.size() != 27)
            throw new IllegalArgumentException("Commit requires five selections and exactly 27 generated positions");
        var rewards = selected.selectedSlots().stream()
                .map(slot -> new CommittedSpaceChestReward(slot, generated.get(slot), false)).toList();
        return new SpaceChestSession(selected.dataVersion(), selected.tier(), SpaceChestPhase.COMMITTED,
                selected.selectedSlots(), rewards);
    }

    public static SpaceChestSession markDelivered(SpaceChestSession current, int index) {
        if (current.phase() != SpaceChestPhase.COMMITTED || index < 0 || index >= current.committedRewards().size())
            return current;
        var reward = current.committedRewards().get(index);
        if (reward.delivered()) return current;
        var updated = new ArrayList<>(current.committedRewards());
        updated.set(index, reward.deliveredCopy());
        return new SpaceChestSession(current.dataVersion(), current.tier(), current.phase(),
                current.selectedSlots(), updated);
    }

    public static int visibleMissedRewards(int elapsedTicks) {
        return Math.min(22, Math.max(0, elapsedTicks) * 22 / SpaceChestSessionService.REVEAL_TICKS);
    }
}
