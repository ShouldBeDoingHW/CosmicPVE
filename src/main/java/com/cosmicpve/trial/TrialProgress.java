package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TrialProgress(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions,
        List<TrialRoomAppearance> appearances, int completedRooms, TrialPhase phase,
        boolean hardcoreBonusApplied, Optional<Identifier> lastRoom, TrialEncounterState encounter) {
    public static final Codec<TrialProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrialPotEntry.CODEC.listOf().optionalFieldOf("pot", List.of()).forGetter(TrialProgress::pot),
            TrialPlayerDecision.CODEC.listOf().optionalFieldOf("decisions", List.of()).forGetter(TrialProgress::decisions),
            TrialRoomAppearance.CODEC.listOf().optionalFieldOf("appearances", List.of()).forGetter(TrialProgress::appearances),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("completed_rooms", 0).forGetter(TrialProgress::completedRooms),
            TrialPhase.CODEC.optionalFieldOf("phase", TrialPhase.APPRENTICE).forGetter(TrialProgress::phase),
            Codec.BOOL.optionalFieldOf("hardcore_bonus_applied", false).forGetter(TrialProgress::hardcoreBonusApplied),
            Identifier.CODEC.optionalFieldOf("last_room").forGetter(TrialProgress::lastRoom),
            TrialEncounterState.CODEC.optionalFieldOf("encounter", TrialEncounterState.EMPTY).forGetter(TrialProgress::encounter)
    ).apply(instance, TrialProgress::new));
    public static final TrialProgress EMPTY = new TrialProgress(List.of(), List.of(), List.of(), 0,
            TrialPhase.APPRENTICE, false, Optional.empty(), TrialEncounterState.EMPTY);
    public TrialProgress { pot = List.copyOf(pot); decisions = List.copyOf(decisions); appearances = List.copyOf(appearances); }

    public TrialDecision decision(UUID player) {
        return decisions.stream().filter(entry -> entry.playerId().equals(player)).map(TrialPlayerDecision::decision)
                .findFirst().orElse(TrialDecision.UNDECIDED);
    }
    public int appearances(Identifier room) {
        return appearances.stream().filter(entry -> entry.roomId().equals(room)).mapToInt(TrialRoomAppearance::count).findFirst().orElse(0);
    }
    public TrialProgress beginDecision(List<UUID> players) {
        return copy(pot, players.stream().map(id -> new TrialPlayerDecision(id, TrialDecision.UNDECIDED)).toList(),
                appearances, completedRooms, phase, hardcoreBonusApplied, lastRoom, TrialEncounterState.EMPTY);
    }
    public TrialProgress decide(UUID player, TrialDecision value) {
        var next = new java.util.ArrayList<>(decisions);
        next.removeIf(entry -> entry.playerId().equals(player)); next.add(new TrialPlayerDecision(player, value));
        return copy(pot, next, appearances, completedRooms, phase, hardcoreBonusApplied, lastRoom, encounter);
    }
    public TrialProgress beginRoom(Identifier room, TrialEncounterState state) {
        var next = new java.util.ArrayList<>(appearances); int count = appearances(room) + 1;
        next.removeIf(entry -> entry.roomId().equals(room)); next.add(new TrialRoomAppearance(room, count));
        return copy(pot, List.of(), next, completedRooms, phase, hardcoreBonusApplied, Optional.of(room), state);
    }
    public TrialProgress completeRoom(List<ItemStack> reward) {
        var nextPot = new java.util.ArrayList<>(pot); nextPot.add(new TrialPotEntry(UUID.randomUUID(), reward));
        int completed = completedRooms + 1; boolean boundary = completed >= 4 && phase == TrialPhase.APPRENTICE;
        return copy(nextPot, decisions, appearances, completed, boundary ? TrialPhase.HARDCORE : phase,
                hardcoreBonusApplied || boundary, lastRoom, TrialEncounterState.EMPTY);
    }
    public TrialProgress withEncounter(TrialEncounterState state) {
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied, lastRoom, state);
    }
    private static TrialProgress copy(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions,
            List<TrialRoomAppearance> appearances, int rooms, TrialPhase phase, boolean bonus,
            Optional<Identifier> last, TrialEncounterState encounter) {
        return new TrialProgress(pot, decisions, appearances, rooms, phase, bonus, last, encounter);
    }
}
