package com.cosmicpve.content.definition.armor;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record ArmorSetDefinitionData(
        Component displayName,
        String color,
        List<Component> fullSetBonus,
        double additiveOutgoingBonus,
        double incomingMultiplier,
        Map<Identifier, Double> procChanceMultipliers,
        List<Identifier> immunities) {
    public static final Codec<ArmorSetDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(ArmorSetDefinitionData::displayName),
            Codec.STRING.fieldOf("color").forGetter(ArmorSetDefinitionData::color),
            ComponentSerialization.CODEC.listOf().fieldOf("full_set_bonus").forGetter(ArmorSetDefinitionData::fullSetBonus),
            Codec.DOUBLE.fieldOf("additive_outgoing_bonus").forGetter(ArmorSetDefinitionData::additiveOutgoingBonus),
            Codec.DOUBLE.fieldOf("incoming_multiplier").forGetter(ArmorSetDefinitionData::incomingMultiplier),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("proc_chance_multipliers", Map.of())
                    .forGetter(ArmorSetDefinitionData::procChanceMultipliers),
            Identifier.CODEC.listOf().optionalFieldOf("immunities", List.of()).forGetter(ArmorSetDefinitionData::immunities)
    ).apply(instance, ArmorSetDefinitionData::new));

    public ValidationResult<ArmorSetDefinition> resolve(Identifier id) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        int parsedColor = parseColor(color, id, diagnostics);
        if (fullSetBonus.isEmpty()) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "full_set_bonus must contain at least one line"));
        }
        if (!Double.isFinite(additiveOutgoingBonus) || additiveOutgoingBonus < -1.0) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "additive_outgoing_bonus must be finite and at least -1"));
        }
        if (!Double.isFinite(incomingMultiplier) || incomingMultiplier < 0.0) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "incoming_multiplier must be finite and non-negative"));
        }
        procChanceMultipliers.forEach((classification, multiplier) -> {
            if (!Double.isFinite(multiplier) || multiplier < 0.0) {
                diagnostics.add(ContentDiagnostic.error(
                        id.toString(), "proc multiplier for " + classification + " must be finite and non-negative"));
            }
        });
        if (new java.util.HashSet<>(immunities).size() != immunities.size()) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "immunities must not contain duplicates"));
        }
        if (!diagnostics.isEmpty()) {
            return ValidationResult.failure(diagnostics);
        }
        return ValidationResult.success(new ArmorSetDefinition(
                id, displayName, parsedColor, fullSetBonus, additiveOutgoingBonus, incomingMultiplier,
                procChanceMultipliers, Set.copyOf(immunities)));
    }

    private static int parseColor(String value, Identifier id, List<ContentDiagnostic> diagnostics) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (!normalized.matches("[0-9A-Fa-f]{6}")) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "color must be a six-digit RGB hex value"));
            return 0;
        }
        return Integer.parseInt(normalized, 16);
    }
}
