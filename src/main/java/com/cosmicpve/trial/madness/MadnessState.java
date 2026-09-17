package com.cosmicpve.trial.madness;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/** Durable pending count, immutable selected-definition snapshots, shared ballot and mutable player votes. */
public record MadnessState(int pending, List<MadnessDefinition> active,
        List<MadnessDefinition> options, List<Vote> votes, long ballotSerial) {
    public static final MadnessState EMPTY = new MadnessState(0, List.of(), List.of(), List.of());
    public static final Codec<MadnessState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 1000000).optionalFieldOf("pending",0).forGetter(MadnessState::pending),
            MadnessDefinition.CODEC.listOf().optionalFieldOf("active",List.of()).forGetter(MadnessState::active),
            MadnessDefinition.CODEC.listOf().optionalFieldOf("options",List.of()).forGetter(MadnessState::options),
            Vote.CODEC.listOf().optionalFieldOf("votes",List.of()).forGetter(MadnessState::votes),
            Codec.LONG.optionalFieldOf("ballot_serial",0L).forGetter(MadnessState::ballotSerial)
    ).apply(i, MadnessState::new));
    public MadnessState {
        active = List.copyOf(active); options = List.copyOf(options); votes = List.copyOf(votes);
        if (pending < 0 || options.size() > 5 || active.stream().map(MadnessDefinition::id).distinct().count() != active.size()
                || options.stream().map(MadnessDefinition::id).distinct().count() != options.size())
            throw new IllegalArgumentException("Invalid Madness state");
    }
    public MadnessState(int pending,List<MadnessDefinition> active,List<MadnessDefinition> options,List<Vote> votes) {
        this(pending,active,options,votes,0L);
    }
    public boolean hasSubmittedVote(UUID player) { return votes.stream().anyMatch(v -> v.player().equals(player)); }
    public boolean owesVote(UUID player) { return !options.isEmpty() && !hasSubmittedVote(player); }
    public boolean allSubmitted(List<UUID> electorate) {
        return !options.isEmpty() && !electorate.isEmpty() && electorate.stream().allMatch(this::hasSubmittedVote);
    }
    public boolean has(String handler) { return active.stream().anyMatch(d -> d.handler().getPath().equals(handler)); }
    public MadnessState queue(int count) { return new MadnessState(Math.addExact(pending,count),active,options,votes,ballotSerial); }
    public MadnessState offer(List<MadnessDefinition> definitions, int count, RandomSource random) {
        if (pending == 0 || !options.isEmpty()) return this;
        var eligible = new ArrayList<>(definitions.stream().filter(MadnessDefinition::enabled)
                .filter(d -> active.stream().noneMatch(a -> a.id().equals(d.id()))).toList());
        var selected = new ArrayList<MadnessDefinition>();
        while (!eligible.isEmpty() && selected.size() < count) selected.add(eligible.remove(random.nextInt(eligible.size())));
        return new MadnessState(pending,active,selected,List.of(),Math.addExact(ballotSerial,1L));
    }
    public MadnessState vote(UUID player, Identifier option) {
        if (options.stream().noneMatch(o -> o.id().equals(option))) throw new IllegalArgumentException("Unknown ballot option");
        var next = new ArrayList<>(votes); next.removeIf(v -> v.player().equals(player)); next.add(new Vote(player,option));
        return new MadnessState(pending,active,options,next,ballotSerial);
    }
    public MadnessState resolve(List<UUID> participants, RandomSource random) {
        if (pending == 0) return this;
        var next = new ArrayList<>(active);
        if (!options.isEmpty()) {
            int highest = -1;
            var tied = new ArrayList<MadnessDefinition>();
            for (var option : options) {
                int count = (int)votes.stream().filter(v -> participants.contains(v.player()) && v.option().equals(option.id())).count();
                if (count > highest) { highest = count; tied.clear(); }
                if (count == highest) tied.add(option);
            }
            next.add(tied.get(random.nextInt(tied.size())));
        }
        return new MadnessState(pending-1,next,List.of(),List.of(),ballotSerial);
    }
    public MadnessState resolveBallot(long expectedSerial,List<UUID> electorate,RandomSource random) {
        return ballotSerial != expectedSerial || options.isEmpty() ? this : resolve(electorate,random);
    }
    public record Vote(UUID player, Identifier option) {
        public static final Codec<Vote> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("player").forGetter(Vote::player),
                Identifier.CODEC.fieldOf("option").forGetter(Vote::option)
        ).apply(i,Vote::new));
    }
}
