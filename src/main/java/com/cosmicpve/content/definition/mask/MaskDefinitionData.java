package com.cosmicpve.content.definition.mask;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record MaskDefinitionData(Component displayName, Component effectSummary, String color,
        String profileTexture, MaskBehavior behavior) {
    public static final Codec<MaskDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MaskDefinitionData::displayName),
            ComponentSerialization.CODEC.fieldOf("effect_summary").forGetter(MaskDefinitionData::effectSummary),
            Codec.STRING.fieldOf("color").forGetter(MaskDefinitionData::color),
            Codec.STRING.fieldOf("profile_texture").forGetter(MaskDefinitionData::profileTexture),
            MaskBehavior.CODEC.fieldOf("behavior").forGetter(MaskDefinitionData::behavior)
    ).apply(instance, MaskDefinitionData::new));

    public ValidationResult<MaskDefinition> resolve(Identifier id) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        String normalized = color.startsWith("#") ? color.substring(1) : color;
        int parsedColor = 0;
        if (!normalized.matches("[0-9A-Fa-f]{6}")) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "color must be a six-digit RGB hex value"));
        } else parsedColor = Integer.parseInt(normalized, 16);
        try {
            String json = new String(Base64.getDecoder().decode(profileTexture), StandardCharsets.UTF_8);
            var root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("textures") || !root.getAsJsonObject("textures").has("SKIN"))
                diagnostics.add(ContentDiagnostic.error(id.toString(), "profile_texture must contain textures.SKIN"));
        } catch (RuntimeException exception) {
            diagnostics.add(ContentDiagnostic.error(id.toString(), "profile_texture must be valid Base64 profile JSON"));
        }
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new MaskDefinition(
                id, displayName.copy(), effectSummary.copy(), parsedColor, profileTexture, behavior));
    }
}
