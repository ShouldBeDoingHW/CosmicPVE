package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfile;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.cosmicpve.content.definition.reward.RewardTable;
import java.util.Map;
import net.minecraft.resources.Identifier;

public record ContentSnapshot(
        long revision,
        Map<Identifier, ScalingProfile> scalingProfiles,
        Map<Identifier, StackDefinition> stackDefinitions,
        Map<Identifier, ArmorSetDefinition> armorSetDefinitions,
        Map<Identifier, RewardTable> rewardTables) {
    public static final ContentSnapshot EMPTY = new ContentSnapshot(0L, Map.of(), Map.of(), Map.of(), Map.of());

    public ContentSnapshot {
        if (revision < 0L) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
        scalingProfiles = Map.copyOf(scalingProfiles);
        stackDefinitions = Map.copyOf(stackDefinitions);
        armorSetDefinitions = Map.copyOf(armorSetDefinitions);
        rewardTables = Map.copyOf(rewardTables);
    }

    public ContentSnapshot(long revision, Map<Identifier, ScalingProfile> scalingProfiles,
            Map<Identifier, StackDefinition> stackDefinitions) {
        this(revision, scalingProfiles, stackDefinitions, Map.of(), Map.of());
    }

    public ContentSnapshot(long revision, Map<Identifier, ScalingProfile> scalingProfiles,
            Map<Identifier, StackDefinition> stackDefinitions,
            Map<Identifier, ArmorSetDefinition> armorSetDefinitions) {
        this(revision, scalingProfiles, stackDefinitions, armorSetDefinitions, Map.of());
    }

    public ContentSnapshot withRevision(long nextRevision) {
        return new ContentSnapshot(nextRevision, scalingProfiles, stackDefinitions, armorSetDefinitions, rewardTables);
    }
}
