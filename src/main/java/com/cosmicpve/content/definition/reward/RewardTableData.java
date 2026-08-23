package com.cosmicpve.content.definition.reward;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

public record RewardTableData(List<RewardEntryData> entries) {
    public static final Codec<RewardTableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RewardEntryData.CODEC.listOf().fieldOf("entries").forGetter(RewardTableData::entries)
    ).apply(instance, RewardTableData::new));

    public ValidationResult<RewardTable> resolve(Identifier id, RegistryAccess registries) {
        var diagnostics = new ArrayList<ContentDiagnostic>();
        var resolved = new ArrayList<RewardEntry>();
        if (entries.isEmpty()) diagnostics.add(ContentDiagnostic.error(id.toString(), "reward table must not be empty"));
        for (int index = 0; index < entries.size(); index++) {
            var result = entries.get(index).resolve(id + " entry " + index, registries);
            diagnostics.addAll(result.diagnostics());
            result.value().ifPresent(resolved::add);
        }
        long total = resolved.stream().mapToLong(RewardEntry::weight).sum();
        if (total > Integer.MAX_VALUE) diagnostics.add(ContentDiagnostic.error(id.toString(), "total weight is too large"));
        if (!diagnostics.isEmpty()) return ValidationResult.failure(diagnostics);
        return ValidationResult.success(new RewardTable(id, resolved, (int) total));
    }
}
