package com.cosmicpve.content.definition.trial;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record TrialRoomDefinitionData(String displayName, TrialRoomCategory category,
                                      List<TrialStructurePiece> pieces, TrialSpawnMarkerRule spawnMarker,
                                      Optional<BlockPos> spawnMarkerPosition,
                                      Optional<Identifier> spawnMarkerReplacement, TrialRoomBounds bounds,
                                      Optional<Identifier> handler, Optional<Identifier> loadout, int baseWeight, boolean enabled) {
    public static final Codec<TrialRoomDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(TrialRoomDefinitionData::displayName),
            TrialRoomCategory.CODEC.fieldOf("category").forGetter(TrialRoomDefinitionData::category),
            TrialStructurePiece.CODEC.listOf().fieldOf("pieces").forGetter(TrialRoomDefinitionData::pieces),
            TrialSpawnMarkerRule.CODEC.fieldOf("spawn_marker").forGetter(TrialRoomDefinitionData::spawnMarker),
            BlockPos.CODEC.optionalFieldOf("spawn_marker_position").forGetter(TrialRoomDefinitionData::spawnMarkerPosition),
            Identifier.CODEC.optionalFieldOf("spawn_marker_replacement").forGetter(TrialRoomDefinitionData::spawnMarkerReplacement),
            TrialRoomBounds.CODEC.fieldOf("bounds").forGetter(TrialRoomDefinitionData::bounds),
            Identifier.CODEC.optionalFieldOf("handler").forGetter(TrialRoomDefinitionData::handler),
            Identifier.CODEC.optionalFieldOf("loadout").forGetter(TrialRoomDefinitionData::loadout),
            Codec.intRange(1,1000).optionalFieldOf("base_weight",5).forGetter(TrialRoomDefinitionData::baseWeight),
            Codec.BOOL.optionalFieldOf("enabled",true).forGetter(TrialRoomDefinitionData::enabled)
    ).apply(instance, TrialRoomDefinitionData::new));

    public TrialRoomDefinitionData(String name, TrialRoomCategory category, List<TrialStructurePiece> pieces,
            TrialSpawnMarkerRule marker, Optional<BlockPos> position, Optional<Identifier> replacement, TrialRoomBounds bounds) {
        this(name,category,pieces,marker,position,replacement,bounds,Optional.empty(),Optional.empty(),5,true);
    }

    public ValidationResult<TrialRoomDefinition> resolve(Identifier id) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        if (displayName.isBlank()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Display name is blank"));
        if (pieces.isEmpty()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Trial room needs at least one structure piece"));
        if (!bounds.valid()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Trial room bounds are inverted"));
        if (spawnMarkerPosition.isPresent() && !bounds.at(BlockPos.ZERO).isInside(spawnMarkerPosition.orElseThrow()))
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Spawn marker position lies outside room bounds"));
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new TrialRoomDefinition(id, displayName, category, pieces, spawnMarker,
                spawnMarkerPosition, spawnMarkerReplacement, bounds, handler.orElse(id),loadout.orElse(id),baseWeight,enabled));
    }
}
