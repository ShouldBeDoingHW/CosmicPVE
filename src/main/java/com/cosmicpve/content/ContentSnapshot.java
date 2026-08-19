package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfile;
import com.cosmicpve.content.definition.stack.StackDefinition;
import java.util.Map;
import net.minecraft.resources.Identifier;

public record ContentSnapshot(
        long revision,
        Map<Identifier, ScalingProfile> scalingProfiles,
        Map<Identifier, StackDefinition> stackDefinitions) {
    public static final ContentSnapshot EMPTY = new ContentSnapshot(0L, Map.of(), Map.of());

    public ContentSnapshot {
        if (revision < 0L) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
        scalingProfiles = Map.copyOf(scalingProfiles);
        stackDefinitions = Map.copyOf(stackDefinitions);
    }

    public ContentSnapshot withRevision(long nextRevision) {
        return new ContentSnapshot(nextRevision, scalingProfiles, stackDefinitions);
    }
}
