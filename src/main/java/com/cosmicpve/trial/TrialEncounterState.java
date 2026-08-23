package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;

public record TrialEncounterState(List<String> hiddenSequence, int sequenceProgress,
        List<String> pillarMaterials, List<BlockPos> claimedTargets, List<String> completedCircuits) {
    public static final Codec<TrialEncounterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("hidden_sequence", List.of()).forGetter(TrialEncounterState::hiddenSequence),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("sequence_progress", 0).forGetter(TrialEncounterState::sequenceProgress),
            Codec.STRING.listOf().optionalFieldOf("pillar_materials", List.of()).forGetter(TrialEncounterState::pillarMaterials),
            BlockPos.CODEC.listOf().optionalFieldOf("claimed_targets", List.of()).forGetter(TrialEncounterState::claimedTargets),
            Codec.STRING.listOf().optionalFieldOf("completed_circuits", List.of()).forGetter(TrialEncounterState::completedCircuits)
    ).apply(instance, TrialEncounterState::new));
    public static final TrialEncounterState EMPTY = new TrialEncounterState(List.of(), 0, List.of(), List.of(), List.of());
    public TrialEncounterState {
        hiddenSequence = List.copyOf(hiddenSequence); pillarMaterials = List.copyOf(pillarMaterials);
        claimedTargets = List.copyOf(claimedTargets); completedCircuits = List.copyOf(completedCircuits);
    }
}
