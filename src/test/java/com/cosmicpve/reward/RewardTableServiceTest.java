package com.cosmicpve.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.content.definition.reward.RewardEntry;
import com.cosmicpve.content.definition.reward.RewardTable;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.validation.ValidationResult;
import java.util.List;
import java.util.Map;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class RewardTableServiceTest {
    @Test void injectedRngMakesWeightedBoundarySelectionDeterministicAndAllowsDuplicates() {
        var first = new RewardEntry(2, 1, 1, new RewardDescriptor.Banknote(100));
        var second = new RewardEntry(3, 1, 1, new RewardDescriptor.Banknote(200));
        var table = new RewardTable(Identifier.parse("cosmicpve:test"), List.of(first, second), 5);
        var firstContext = new RewardGenerationContext(RegistryAccess.EMPTY, RandomSource.create(1234), null);
        var secondContext = new RewardGenerationContext(RegistryAccess.EMPTY, RandomSource.create(1234), null);
        var selected = java.util.stream.IntStream.range(0, 20)
                .mapToObj(ignored -> RewardTableService.select(table, firstContext)).toList();
        var repeated = java.util.stream.IntStream.range(0, 20)
                .mapToObj(ignored -> RewardTableService.select(table, secondContext)).toList();
        assertEquals(repeated, selected);
        assertEquals(2, selected.stream().distinct().count());
    }

    @Test void quantityRollsGenerateEachRequestedRewardIndependently() {
        var entry = new RewardEntry(1, 3, 3, new RewardDescriptor.Banknote(12_345));
        var table = new RewardTable(Identifier.parse("cosmicpve:quantity"), List.of(entry), 1);
        var repository = new CosmicContentRepository();
        repository.publish(ValidationResult.success(new ContentSnapshot(0, Map.of(), Map.of(), Map.of(),
                Map.of(table.id(), table))));
        var rewards = new RewardTableService(repository, new RewardGeneratorService()).roll(table.id(), 2,
                new RewardGenerationContext(RegistryAccess.EMPTY, RandomSource.create(9), null));
        assertEquals(6, rewards.size());
        rewards.forEach(stack -> assertEquals(12_345,
                stack.get(ModDataComponents.BANKNOTE.get()).valueCents()));
    }
}
