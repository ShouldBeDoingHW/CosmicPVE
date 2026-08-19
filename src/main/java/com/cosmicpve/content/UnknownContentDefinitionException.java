package com.cosmicpve.content;

import net.minecraft.resources.Identifier;

public final class UnknownContentDefinitionException extends IllegalArgumentException {
    public UnknownContentDefinitionException(String definitionType, Identifier id) {
        super("Unknown " + definitionType + " definition: " + id);
    }
}
