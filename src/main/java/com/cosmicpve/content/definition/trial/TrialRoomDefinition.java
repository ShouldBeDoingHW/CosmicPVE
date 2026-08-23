package com.cosmicpve.content.definition.trial;

import java.util.List;
import net.minecraft.resources.Identifier;

public record TrialRoomDefinition(Identifier id, String displayName, TrialRoomCategory category,
                                  List<TrialStructurePiece> pieces, TrialSpawnMarkerRule spawnMarker,
                                  TrialRoomBounds bounds) {
    public TrialRoomDefinition { pieces = List.copyOf(pieces); }
}
