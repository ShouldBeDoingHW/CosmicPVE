package com.cosmicpve.content.definition.stack;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public record StackDefinitionData(
        StackPolarity polarity,
        int maximumStacks,
        int durationTicks,
        StackRefreshPolicy refreshPolicy,
        boolean transferable,
        boolean cleansable,
        boolean persistent) {
    public static final int MAXIMUM_STACK_LIMIT = 1_024;
    public static final int MAXIMUM_DURATION_TICKS = 1_728_000;

    public static final Codec<StackDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StackPolarity.CODEC.fieldOf("polarity").forGetter(StackDefinitionData::polarity),
            Codec.INT.fieldOf("maximum_stacks").forGetter(StackDefinitionData::maximumStacks),
            Codec.INT.fieldOf("duration_ticks").forGetter(StackDefinitionData::durationTicks),
            StackRefreshPolicy.CODEC.fieldOf("refresh_policy").forGetter(StackDefinitionData::refreshPolicy),
            Codec.BOOL.fieldOf("transferable").forGetter(StackDefinitionData::transferable),
            Codec.BOOL.fieldOf("cleansable").forGetter(StackDefinitionData::cleansable),
            Codec.BOOL.fieldOf("persistent").forGetter(StackDefinitionData::persistent)
    ).apply(instance, StackDefinitionData::new));

    public ValidationResult<StackDefinition> resolve(Identifier id) {
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        if (maximumStacks < 1 || maximumStacks > MAXIMUM_STACK_LIMIT) {
            diagnostics.add(ContentDiagnostic.error(
                    id.toString(),
                    "maximum_stacks must be between 1 and " + MAXIMUM_STACK_LIMIT));
        }
        if (durationTicks < 1 || durationTicks > MAXIMUM_DURATION_TICKS) {
            diagnostics.add(ContentDiagnostic.error(
                    id.toString(),
                    "duration_ticks must be between 1 and " + MAXIMUM_DURATION_TICKS));
        }
        if (refreshPolicy == StackRefreshPolicy.INDEPENDENT && maximumStacks == 1) {
            diagnostics.add(ContentDiagnostic.error(
                    id.toString(),
                    "refresh_policy independent requires maximum_stacks greater than 1"));
        }

        if (!diagnostics.isEmpty()) {
            return ValidationResult.failure(diagnostics);
        }
        return ValidationResult.success(new StackDefinition(
                id,
                polarity,
                maximumStacks,
                durationTicks,
                refreshPolicy,
                transferable,
                cleansable,
                persistent));
    }
}
