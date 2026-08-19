package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfile;
import com.cosmicpve.content.definition.scaling.ScalingProfileData;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.stack.StackDefinitionData;
import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class ContentCandidateBuilder {
    private final Map<Identifier, ScalingProfile> scalingProfiles = new LinkedHashMap<>();
    private final Map<Identifier, StackDefinition> stackDefinitions = new LinkedHashMap<>();
    private final List<ContentDiagnostic> diagnostics = new ArrayList<>();

    public void addScalingProfile(Identifier id, ScalingProfileData data) {
        ValidationResult<ScalingProfile> result = data.resolve(id);
        if (!result.isSuccess()) {
            diagnostics.addAll(result.diagnostics());
            return;
        }
        if (scalingProfiles.putIfAbsent(id, result.valueOrThrow()) != null) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate scaling profile ID"));
        }
    }

    public void addStackDefinition(Identifier id, StackDefinitionData data) {
        ValidationResult<StackDefinition> result = data.resolve(id);
        if (!result.isSuccess()) {
            diagnostics.addAll(result.diagnostics());
            return;
        }
        if (stackDefinitions.putIfAbsent(id, result.valueOrThrow()) != null) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate stack definition ID"));
        }
    }

    public void addDiagnostic(ContentDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public ValidationResult<ContentSnapshot> build() {
        if (ValidationResult.hasErrors(diagnostics)) {
            return ValidationResult.failure(diagnostics);
        }
        return ValidationResult.success(
                new ContentSnapshot(0L, scalingProfiles, stackDefinitions),
                diagnostics);
    }
}
