package com.cosmicpve.content.definition.trial;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public record TrialRoomDefinitionData(String displayName, TrialRoomCategory category,
                                      List<TrialStructurePiece> pieces, TrialSpawnMarkerRule spawnMarker,
                                      TrialRoomBounds bounds) {
    public static final Codec<TrialRoomDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(TrialRoomDefinitionData::displayName),
            TrialRoomCategory.CODEC.fieldOf("category").forGetter(TrialRoomDefinitionData::category),
            TrialStructurePiece.CODEC.listOf().fieldOf("pieces").forGetter(TrialRoomDefinitionData::pieces),
            TrialSpawnMarkerRule.CODEC.fieldOf("spawn_marker").forGetter(TrialRoomDefinitionData::spawnMarker),
            TrialRoomBounds.CODEC.fieldOf("bounds").forGetter(TrialRoomDefinitionData::bounds)
    ).apply(instance, TrialRoomDefinitionData::new));

    public ValidationResult<TrialRoomDefinition> resolve(Identifier id) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        if (displayName.isBlank()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Display name is blank"));
        if (pieces.isEmpty()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Trial room needs at least one structure piece"));
        if (!bounds.valid()) diagnostics.add(ContentDiagnostic.error(id.toString(), "Trial room bounds are inverted"));
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new TrialRoomDefinition(id, displayName, category, pieces, spawnMarker, bounds));
    }
}
