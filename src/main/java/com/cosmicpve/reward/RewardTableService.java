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
        return roll(repository.requireRewardTable(tableId), rolls, context);
    }

    /** Ephemeral per-completion rows never mutate the shared data-driven reward catalog. */
    public List<ItemStack> rollWithExtra(Identifier tableId, RewardEntry extra, int rolls, RewardGenerationContext context) {
        return roll(withExtra(repository.requireRewardTable(tableId), extra), rolls, context);
    }

    public static RewardTable withExtra(RewardTable base, RewardEntry extra) {
        var rows = new ArrayList<>(base.entries());
        rows.add(extra);
        return new RewardTable(base.id(), rows, base.totalWeight() + extra.weight());
    }

    private List<ItemStack> roll(RewardTable table, int rolls, RewardGenerationContext context) {
        if (rolls < 1) throw new IllegalArgumentException("rolls must be positive");
        var results = new ArrayList<ItemStack>();
        for (int roll = 0; roll < rolls; roll++) {
            var bundle = new ArrayList<ItemStack>();
            RewardEntry selected = select(table, context);
            int quantity = context.random().nextIntBetweenInclusive(
                    selected.minimumQuantity(), selected.maximumQuantity());
            for (int index = 0; index < quantity; index++) {
                generator.generate(selected.reward(), context).ifPresent(bundle::add);
            }
            results.addAll(compact(bundle));
        }
        return List.copyOf(results);
    }

    /** Compacts identical stackable results inside one reward position without merging independent generated books. */
    public static List<ItemStack> compact(List<ItemStack> generated) {
        var compacted = new ArrayList<ItemStack>();
        for (ItemStack original : generated) {
            ItemStack remaining = original.copy();
            for (ItemStack existing : compacted) {
                if (remaining.isEmpty()) break;
                if (ItemStack.isSameItemSameComponents(existing, remaining) && existing.getCount() < existing.getMaxStackSize()) {
                    int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
            while (!remaining.isEmpty()) {
                int count = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack part = remaining.copyWithCount(count);
                compacted.add(part);
                remaining.shrink(count);
            }
        }
        return List.copyOf(compacted);
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
