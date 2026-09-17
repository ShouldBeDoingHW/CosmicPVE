package com.cosmicpve.content.definition.trial;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record TrialRoomDefinition(Identifier id, String displayName, TrialRoomCategory category,
                                  List<TrialStructurePiece> pieces, TrialSpawnMarkerRule spawnMarker,
                                  Optional<BlockPos> spawnMarkerPosition,
                                  Optional<Identifier> spawnMarkerReplacement, TrialRoomBounds bounds,
                                  Identifier handler, Identifier loadout, int baseWeight, boolean enabled) {
    public TrialRoomDefinition { pieces = List.copyOf(pieces); }
    public TrialRoomDefinition(Identifier id, String name, TrialRoomCategory category, List<TrialStructurePiece> pieces,
            TrialSpawnMarkerRule marker, Optional<BlockPos> position, Optional<Identifier> replacement, TrialRoomBounds bounds) {
        this(id,name,category,pieces,marker,position,replacement,bounds,id,id,5,true);
    }
}
