package com.cosmicpve.content.definition.scaling;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public record ScalingProfileData(
        double onePlayer,
        double twoPlayers,
        double threePlayers,
        double fourPlayers) {
    public static final Codec<ScalingProfileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("party_size_1").forGetter(ScalingProfileData::onePlayer),
            Codec.DOUBLE.fieldOf("party_size_2").forGetter(ScalingProfileData::twoPlayers),
            Codec.DOUBLE.fieldOf("party_size_3").forGetter(ScalingProfileData::threePlayers),
            Codec.DOUBLE.fieldOf("party_size_4").forGetter(ScalingProfileData::fourPlayers)
    ).apply(instance, ScalingProfileData::new));

    public ValidationResult<ScalingProfile> resolve(Identifier id) {
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        validateValue(id, 1, onePlayer, diagnostics);
        validateValue(id, 2, twoPlayers, diagnostics);
        validateValue(id, 3, threePlayers, diagnostics);
        validateValue(id, 4, fourPlayers, diagnostics);

        if (!diagnostics.isEmpty()) {
            return ValidationResult.failure(diagnostics);
        }
        return ValidationResult.success(new ScalingProfile(id, onePlayer, twoPlayers, threePlayers, fourPlayers));
    }

    private static void validateValue(
            Identifier id,
            int partySize,
            double value,
            List<ContentDiagnostic> diagnostics) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            diagnostics.add(ContentDiagnostic.error(
                    id.toString(),
                    "party_size_" + partySize + " must be a finite value greater than zero"));
        }
    }
}
