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
        TrialPortalModifiers portalModifiers, boolean initialSkipProcessed, long baseFame,
        com.cosmicpve.trial.madness.MadnessState madness) {
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
            Codec.BOOL.optionalFieldOf("initial_skip_processed", false).forGetter(TrialProgress::initialSkipProcessed),
            Codec.LONG.optionalFieldOf("base_fame", 0L).forGetter(TrialProgress::baseFame),
            com.cosmicpve.trial.madness.MadnessState.CODEC.optionalFieldOf("madness", com.cosmicpve.trial.madness.MadnessState.EMPTY).forGetter(TrialProgress::madness)
    ).apply(instance, TrialProgress::new));
    public static final TrialProgress EMPTY = new TrialProgress(List.of(), List.of(), List.of(), 0,
            TrialPhase.APPRENTICE, false, false, Optional.empty(), TrialEncounterState.EMPTY,
            TrialPortalModifiers.EMPTY, false, 0L);
    public static TrialProgress initial(TrialPortalModifiers modifiers) {
        return new TrialProgress(List.of(), List.of(), List.of(), 0, TrialPhase.APPRENTICE,
                false, false, Optional.empty(), TrialEncounterState.EMPTY, modifiers, false, 0L);
    }
    public TrialProgress {
        pot = List.copyOf(pot); decisions = List.copyOf(decisions); appearances = List.copyOf(appearances);
        phase = TrialProgression.phaseAfter(completedRooms); // Serialized legacy phase is never a second authority.
    }

    public TrialProgress(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions, List<TrialRoomAppearance> appearances,
            int completed, TrialPhase phase, boolean hardcore, boolean demonic, Optional<Identifier> last,
            TrialEncounterState encounter, TrialPortalModifiers modifiers, boolean skipped, long fame) {
        this(pot, decisions, appearances, completed, phase, hardcore, demonic, last, encounter, modifiers, skipped,
                fame, com.cosmicpve.trial.madness.MadnessState.EMPTY);
    }
    public TrialProgress withMadness(com.cosmicpve.trial.madness.MadnessState state) {
        return new TrialProgress(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, initialSkipProcessed, baseFame, state);
    }

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
                portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress decide(UUID player, TrialDecision value) {
        var next = new java.util.ArrayList<>(decisions);
        next.removeIf(entry -> entry.playerId().equals(player)); next.add(new TrialPlayerDecision(player, value));
        return copy(pot, next, appearances, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, lastRoom, encounter,
                portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress beginRoom(Identifier room, TrialEncounterState state) {
        var next = new java.util.ArrayList<>(appearances); int count = appearances(room) + 1;
        next.removeIf(entry -> entry.roomId().equals(room)); next.add(new TrialRoomAppearance(room, count));
        return copy(pot, List.of(), next, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, Optional.of(room), state,
                portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress completeRoom(List<ItemStack> reward) {
        return completeRoom(reward, 0L);
    }
    public TrialProgress completeRoom(List<ItemStack> reward, long fameAward) {
        if (fameAward < 0) throw new IllegalArgumentException("Fame award cannot be negative");
        var nextPot = new java.util.ArrayList<>(pot); nextPot.add(new TrialPotEntry(UUID.randomUUID(), reward,Optional.of(phase),false));
        int completed = completedRooms + 1;
        boolean hardcoreBoundary = completedRooms < 4 && completed >= 4;
        boolean demonicBoundary = completedRooms < 12 && completed >= 12;
        TrialPhase nextPhase = TrialProgression.phaseAfter(completed);
        return copy(nextPot, decisions, appearances, completed, nextPhase,
                hardcoreBonusApplied || hardcoreBoundary, demonicBonusApplied || demonicBoundary,
                lastRoom, TrialEncounterState.EMPTY, portalModifiers, initialSkipProcessed,
                Math.addExact(baseFame, fameAward));
    }
    public TrialProgress appendSkippedReward(List<ItemStack> reward) {
        if (initialSkipProcessed) throw new IllegalStateException("Initial Skip is already committed");
        var nextPot = new java.util.ArrayList<>(pot); nextPot.add(new TrialPotEntry(UUID.randomUUID(), reward,
                Optional.of(TrialProgression.phaseForRoomOrdinal(nextRoomOrdinal())),true));
        int completed = Math.addExact(completedRooms, 1);
        return copy(nextPot, decisions, appearances, completed, TrialProgression.phaseAfter(completed),
                hardcoreBonusApplied || completed >= 4, demonicBonusApplied || completed >= 12,
                lastRoom, encounter, portalModifiers, false, baseFame);
    }
    public TrialProgress markInitialSkipProcessed() {
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, true, baseFame);
    }
    public TrialProgress withEncounter(TrialEncounterState state) {
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied, demonicBonusApplied, lastRoom, state,
                portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress debugSetCompletedRooms(int rooms) {
        if (rooms < 0 || rooms > 10_000) throw new IllegalArgumentException("rooms must be in [0,10000]");
        TrialPhase nextPhase = phaseForCompletedRooms(rooms);
        return copy(pot, decisions, appearances, rooms, nextPhase, rooms >= 4, rooms >= 12, lastRoom, encounter,
                portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress debugEnterPhase(TrialPhase target) {
        int rooms = phase == target ? completedRooms : switch (target) {
            case APPRENTICE -> 0; case HARDCORE -> 4; case IMPOSSIBLE -> 8; case DEMONIC -> 12;
        };
        return copy(pot, decisions, appearances, rooms, target,
                hardcoreBonusApplied || target != TrialPhase.APPRENTICE,
                demonicBonusApplied || target == TrialPhase.DEMONIC, lastRoom, encounter, portalModifiers, initialSkipProcessed, baseFame);
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
                demonicBonusApplied, lastRoom, encounter, portalModifiers, initialSkipProcessed, baseFame);
    }
    public TrialProgress debugSetBaseFame(long fame) {
        if (fame < 0) throw new IllegalArgumentException("Fame cannot be negative");
        return copy(pot, decisions, appearances, completedRooms, phase, hardcoreBonusApplied,
                demonicBonusApplied, lastRoom, encounter, portalModifiers, initialSkipProcessed, fame);
    }
    private TrialProgress copy(List<TrialPotEntry> pot, List<TrialPlayerDecision> decisions,
            List<TrialRoomAppearance> appearances, int rooms, TrialPhase phase, boolean hardcoreBonus,
            boolean demonicBonus,
            Optional<Identifier> last, TrialEncounterState encounter, TrialPortalModifiers modifiers, boolean skipProcessed,
            long baseFame) {
        return new TrialProgress(pot, decisions, appearances, rooms, phase, hardcoreBonus, demonicBonus, last, encounter,
                modifiers, skipProcessed, baseFame, madness.queue(
                        rooms >= completedRooms ? TrialProgression.crossedMadnessThresholds(completedRooms, rooms) : 0));
    }

    public static TrialPhase phaseForCompletedRooms(int completedRooms) {
        return TrialProgression.phaseAfter(completedRooms);
    }

    public int nextRoomOrdinal() { return Math.addExact(completedRooms, 1); }
    public boolean impossibleBonusApplied() { return completedRooms >= 8; }
}
