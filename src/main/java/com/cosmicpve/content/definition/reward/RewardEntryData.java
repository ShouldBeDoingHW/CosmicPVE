package com.cosmicpve.content.definition.reward;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;

public record RewardEntryData(int weight, int minimumQuantity, int maximumQuantity, RewardDescriptorData reward) {
    public static final Codec<RewardEntryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("weight").forGetter(RewardEntryData::weight),
            Codec.INT.optionalFieldOf("minimum_quantity", 1).forGetter(RewardEntryData::minimumQuantity),
            Codec.INT.optionalFieldOf("maximum_quantity", 1).forGetter(RewardEntryData::maximumQuantity),
            RewardDescriptorData.CODEC.fieldOf("reward").forGetter(RewardEntryData::reward)
    ).apply(instance, RewardEntryData::new));

    ValidationResult<RewardEntry> resolve(String source, RegistryAccess registries) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        if (weight <= 0) diagnostics.add(ContentDiagnostic.error(source, "weight must be positive"));
        if (minimumQuantity < 1 || maximumQuantity < minimumQuantity)
            diagnostics.add(ContentDiagnostic.error(source, "quantity range must satisfy 1 <= minimum <= maximum"));
        var descriptor = reward.resolve(source, registries);
        diagnostics.addAll(descriptor.diagnostics());
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new RewardEntry(weight, minimumQuantity, maximumQuantity, descriptor.valueOrThrow()));
    }
}
