package com.cosmicpve.content.definition.mask;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Immutable, resolved runtime mask definition. */
public record MaskDefinition(Identifier id, Component displayName, Component effectSummary,
        int presentationColor, String profileTexture, MaskBehavior behavior) {}
