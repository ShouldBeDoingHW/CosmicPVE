package com.cosmicpve.content.definition.trial;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record TrialRoomDefinition(Identifier id, String displayName, TrialRoomCategory category,
                                  List<TrialStructurePiece> pieces, TrialSpawnMarkerRule spawnMarker,
                                  Optional<BlockPos> spawnMarkerPosition,
                                  Optional<Identifier> spawnMarkerReplacement, TrialRoomBounds bounds) {
    public TrialRoomDefinition { pieces = List.copyOf(pieces); }
}
