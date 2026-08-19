package com.cosmicpve.content.definition.stack;

import net.minecraft.resources.Identifier;

public record StackDefinition(
        Identifier id,
        StackPolarity polarity,
        int maximumStacks,
        int durationTicks,
        StackRefreshPolicy refreshPolicy,
        boolean transferable,
        boolean cleansable,
        boolean persistent) {}
