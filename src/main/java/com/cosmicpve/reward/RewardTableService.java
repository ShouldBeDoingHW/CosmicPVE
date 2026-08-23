package com.cosmicpve.reward;

import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.reward.RewardEntry;
import com.cosmicpve.content.definition.reward.RewardTable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class RewardTableService {
    private final CosmicContentRepository repository;
    private final RewardGeneratorService generator;

    public RewardTableService(CosmicContentRepository repository, RewardGeneratorService generator) {
        this.repository = repository;
        this.generator = generator;
    }

    public List<ItemStack> roll(Identifier tableId, int rolls, RewardGenerationContext context) {
        if (rolls < 1) throw new IllegalArgumentException("rolls must be positive");
        RewardTable table = repository.requireRewardTable(tableId);
        var results = new ArrayList<ItemStack>();
        for (int roll = 0; roll < rolls; roll++) {
            RewardEntry selected = select(table, context);
            int quantity = context.random().nextIntBetweenInclusive(
                    selected.minimumQuantity(), selected.maximumQuantity());
            for (int index = 0; index < quantity; index++) {
                generator.generate(selected.reward(), context).ifPresent(results::add);
            }
        }
        return List.copyOf(results);
    }

    public static RewardEntry select(RewardTable table, RewardGenerationContext context) {
        int target = context.random().nextInt(table.totalWeight());
        int cursor = 0;
        for (RewardEntry entry : table.entries()) {
            cursor += entry.weight();
            if (target < cursor) return entry;
        }
        throw new IllegalStateException("Validated reward table had no selectable entry: " + table.id());
    }
}
