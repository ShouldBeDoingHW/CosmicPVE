package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfile;
import com.cosmicpve.content.definition.scaling.ScalingProfileData;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.stack.StackDefinitionData;
import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.cosmicpve.content.definition.armor.ArmorSetDefinitionData;
import com.cosmicpve.content.definition.reward.RewardTable;
import com.cosmicpve.content.definition.reward.RewardTableData;
import com.cosmicpve.content.definition.trial.TrialRoomDefinition;
import com.cosmicpve.content.definition.trial.TrialRoomDefinitionData;
import com.cosmicpve.content.definition.mask.MaskDefinition;
import com.cosmicpve.content.definition.mask.MaskDefinitionData;
import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.core.RegistryAccess;

public final class ContentCandidateBuilder {
    private final Map<Identifier, ScalingProfile> scalingProfiles = new LinkedHashMap<>();
    private final Map<Identifier, StackDefinition> stackDefinitions = new LinkedHashMap<>();
    private final Map<Identifier, ArmorSetDefinition> armorSetDefinitions = new LinkedHashMap<>();
    private final Map<Identifier, RewardTable> rewardTables = new LinkedHashMap<>();
    private final Map<Identifier, TrialRoomDefinition> trialRooms = new LinkedHashMap<>();
    private final Map<Identifier, MaskDefinition> maskDefinitions = new LinkedHashMap<>();
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

    public void addArmorSetDefinition(Identifier id, ArmorSetDefinitionData data) {
        ValidationResult<ArmorSetDefinition> result = data.resolve(id);
        if (!result.isSuccess()) {
            diagnostics.addAll(result.diagnostics());
            return;
        }
        if (armorSetDefinitions.putIfAbsent(id, result.valueOrThrow()) != null) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate armor-set definition ID"));
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

    public void addRewardTable(Identifier id, RewardTableData data, RegistryAccess registries) {
        ValidationResult<RewardTable> result = data.resolve(id, registries);
        if (!result.isSuccess()) {
            diagnostics.addAll(result.diagnostics());
            return;
        }
        if (rewardTables.putIfAbsent(id, result.valueOrThrow()) != null) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate reward table ID"));
        }
    }

    public void addTrialRoom(Identifier id, TrialRoomDefinitionData data) {
        ValidationResult<TrialRoomDefinition> result = data.resolve(id);
        if (!result.isSuccess()) { diagnostics.addAll(result.diagnostics()); return; }
        if (trialRooms.putIfAbsent(id, result.valueOrThrow()) != null) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate Trial room ID"));
        }
    }

    public void addMaskDefinition(Identifier id, MaskDefinitionData data) {
        ValidationResult<MaskDefinition> result = data.resolve(id);
        if (!result.isSuccess()) { diagnostics.addAll(result.diagnostics()); return; }
        if (maskDefinitions.putIfAbsent(id, result.valueOrThrow()) != null)
            diagnostics.add(ContentDiagnostic.error(id.toString(), "Duplicate mask definition ID"));
    }

    public void addDiagnostic(ContentDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public ValidationResult<ContentSnapshot> build() {
        var behaviorOwners = new java.util.EnumMap<com.cosmicpve.content.definition.mask.MaskBehavior, Identifier>(
                com.cosmicpve.content.definition.mask.MaskBehavior.class);
        maskDefinitions.forEach((id, definition) -> {
            Identifier previous = behaviorOwners.putIfAbsent(definition.behavior(), id);
            if (previous != null) diagnostics.add(ContentDiagnostic.error(id.toString(),
                    "Mask behavior " + definition.behavior() + " is already owned by " + previous));
        });
        rewardTables.forEach((tableId, table) -> table.entries().forEach(entry -> {
            if (entry.reward() instanceof com.cosmicpve.content.definition.reward.RewardDescriptor.Mask mask
                    && mask.maskCount() > maskDefinitions.size())
                diagnostics.add(ContentDiagnostic.error(tableId.toString(),
                        "Mask reward requests " + mask.maskCount() + " distinct masks but only " + maskDefinitions.size() + " are defined"));
            if (entry.reward() instanceof com.cosmicpve.content.definition.reward.RewardDescriptor.ArmorSetCrystal crystal
                    && !armorSetDefinitions.containsKey(crystal.armorSetId()))
                diagnostics.add(ContentDiagnostic.error(tableId.toString(), "Unknown armor set: " + crystal.armorSetId()));
        }));
        if (ValidationResult.hasErrors(diagnostics)) {
            return ValidationResult.failure(diagnostics);
        }
        return ValidationResult.success(
                new ContentSnapshot(0L, scalingProfiles, stackDefinitions, armorSetDefinitions, rewardTables, trialRooms, maskDefinitions),
                diagnostics);
    }
}
