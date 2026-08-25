package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

/** Resolved player-facing mask data synchronized with the stack for remote clients. */
public record MaskPresentation(Identifier id, Component displayName, Component effectSummary, int color) {
    public static final Codec<MaskPresentation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(MaskPresentation::id),
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MaskPresentation::displayName),
            ComponentSerialization.CODEC.fieldOf("effect_summary").forGetter(MaskPresentation::effectSummary),
            Codec.INT.fieldOf("color").forGetter(MaskPresentation::color)
    ).apply(instance, MaskPresentation::new));
}
