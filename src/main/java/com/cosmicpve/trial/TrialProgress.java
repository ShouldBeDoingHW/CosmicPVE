package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.cosmicpve.data.component.TrialPortalModifiers;

public record TrialProgress(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions,
        List<TrialRoomAppearance> appearances, int completedRooms, TrialPhase phase,
        boolean hardcoreBonusApplied, boolean demonicBonusApplied,
        Optional<Identifier> lastRoom, TrialEncounterState encounter,
        TrialPortalModifiers portalModifiers, boolean initialSkipProcessed) {
    public static final Codec<TrialProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrialPotEntry.CODEC.listOf().optionalFieldOf("pot", List.of()).forGetter(TrialProgress::pot),
            TrialPlayerDecision.CODEC.listOf().optionalFieldOf("decisions", List.of()).forGetter(TrialProgress::decisions),
            TrialRoomAppearance.CODEC.listOf().optionalFieldOf("appearances", List.of()).forGetter(TrialProgress::appearances),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("completed_rooms", 0).forGetter(TrialProgress::completedRooms),
            TrialPhase.CODEC.optionalFieldOf("phase", TrialPhase.APPRENTICE).forGetter(TrialProgress::phase),
            Codec.BOOL.optionalFieldOf("hardcore_bonus_applied", false).forGetter(TrialProgress::hardcoreBonusApplied),
            Codec.BOOL.optionalFieldOf("demonic_bonus_applied", false).forGetter(TrialProgress::demonicBonusApplied),
            Identifier.CODEC.optionalFieldOf("last_room").forGetter(TrialProgress::lastRoom),
            TrialEncounterState.CODEC.optionalFieldOf("encounter", TrialEncounterState.EMPTY).forGetter(TrialProgress::encounter),
            TrialPortalModifiers.CODEC.optionalFieldOf("portal_modifiers", TrialPortalModifiers.EMPTY).forGetter(TrialProgress::portalModifiers),
            Codec.BOOL.optionalFieldOf("initial_skip_processed", false).forGetter(TrialProgress::initialSkipProcessed)
    ).apply(instance, TrialProgress::new));
    public static final TrialProgress EMPTY = new TrialProgress(List.of(), List.of(), List.of(), 0,
            TrialPhase.APPRENTICE, false, false, Optional.empty(), TrialEncounterState.EMPTY,
            TrialPortalModifiers.EMPTY, false);
    public static TrialProgress initial(TrialPortalModifiers modifiers) {
        return new TrialProgress(List.of(), List.of(), List.of(), 0, TrialPhase.APPRENTICE,
                false, false, Optional.empty(), TrialEncounterState.EMPTY, modifiers, false);
    }
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
                appearances, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, lastRoom, TrialEncounterState.EMPTY,
                portalModifiers, initialSkipProcessed);
    }
    public TrialProgress decide(UUID player, TrialDecision value) {
        var next = new java.util.ArrayList<>(decisions);
        next.removeIf(entry -> entry.playerId().equals(player)); next.add(new TrialPlayerDecision(player, value));
        return copy(pot, next, appearances, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, lastRoom, encounter,
                portalModifiers, initialSkipProcessed);
    }
    public TrialProgress beginRoom(Identifier room, TrialEncounterState state) {
        var next = new java.util.ArrayList<>(appearances); int count = appearances(room) + 1;
        next.removeIf(entry -> entry.roomId().equals(room)); next.add(new TrialRoomAppearance(room, count));
        return copy(pot, List.of(), next, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, Optional.of(room), state,
                portalModifiers, initialSkipProcessed);
    }
    public TrialProgress completeRoom(List<ItemStack> reward) {
        var nextPot = new java.util.ArrayList<>(pot); nextPot.add(new TrialPotEntry(UUID.randomUUID(), reward));
        int completed = completedRooms + 1;
        boolean hardcoreBoundary = completed >= 4 && phase == TrialPhase.APPRENTICE;
        boolean impossibleBoundary = completed >= 8 && phase == TrialPhase.HARDCORE;
        boolean demonicBoundary = completed >= 12 && phase == TrialPhase.IMPOSSIBLE;
        TrialPhase nextPhase = demonicBoundary ? TrialPhase.DEMONIC
                : impossibleBoundary ? TrialPhase.IMPOSSIBLE
                : hardcoreBoundary ? TrialPhase.HARDCORE : phase;
        return copy(nextPot, decisions, appearances, completed, nextPhase,
                hardcoreBonusApplied || hardcoreBoundary, demonicBonusApplied || demonicBoundary,
                lastRoom, TrialEncounterState.EMPTY, portalModifiers, initialSkipProcessed);
    }
    public TrialProgress appendSkippedReward(List<ItemStack> reward) {
        if (phase != TrialPhase.APPRENTICE || completedRooms >= 3)
            throw new IllegalStateException("Initial Skip can advance only the first three Apprentice rooms");
        var nextPot = new java.util.ArrayList<>(pot); nextPot.add(new TrialPotEntry(UUID.randomUUID(), reward));
        return copy(nextPot, decisions, appearances, completedRooms + 1, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, false);
    }
    public TrialProgress markInitialSkipProcessed() {
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, true);
    }
    public TrialProgress withEncounter(TrialEncounterState state) {
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, lastRoom, state,
                portalModifiers, initialSkipProcessed);
    }
    public TrialProgress debugSetCompletedRooms(int rooms) {
        if (rooms < 0 || rooms > 10_000) throw new IllegalArgumentException("rooms must be in [0,10000]");
        TrialPhase nextPhase = phaseForCompletedRooms(rooms);
        return copy(pot, decisions, appearances, rooms, nextPhase, rooms >= 4, rooms >= 12, lastRoom, encounter,
                portalModifiers, initialSkipProcessed);
    }
    public TrialProgress debugEnterPhase(TrialPhase target) {
        return copy(pot, decisions, appearances, completedRooms, target,
                hardcoreBonusApplied || target != TrialPhase.APPRENTICE,
                demonicBonusApplied || target == TrialPhase.DEMONIC, lastRoom, encounter, portalModifiers, initialSkipProcessed);
    }
    public TrialProgress debugFillPot(int count) {
        if (count < 0 || count > 1000) throw new IllegalArgumentException("pot count must be in [0,1000]");
        var entries = new java.util.ArrayList<TrialPotEntry>();
        for (int i = 0; i < count; i++) {
            ItemStack item = new ItemStack(net.minecraft.world.item.Items.PAPER);
            item.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    net.minecraft.network.chat.Component.literal("Development Reward " + (i + 1)));
            entries.add(new TrialPotEntry(UUID.randomUUID(), List.of(item)));
        }
        return copy(entries, decisions, appearances, completedRooms, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, initialSkipProcessed);
    }
    private static TrialProgress copy(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions,
            List<TrialRoomAppearance> appearances, int rooms, TrialPhase phase, boolean hardcoreBonus,
            boolean demonicBonus,
            Optional<Identifier> last, TrialEncounterState encounter, TrialPortalModifiers modifiers, boolean skipProcessed) {
        return new TrialProgress(pot, decisions, appearances, rooms, phase, hardcoreBonus, demonicBonus, last, encounter,
                modifiers, skipProcessed);
    }

    public static TrialPhase phaseForCompletedRooms(int completedRooms) {
        if (completedRooms < 0) throw new IllegalArgumentException("completedRooms cannot be negative");
        if (completedRooms >= 12) return TrialPhase.DEMONIC;
        if (completedRooms >= 8) return TrialPhase.IMPOSSIBLE;
        if (completedRooms >= 4) return TrialPhase.HARDCORE;
        return TrialPhase.APPRENTICE;
    }
}
