package com.cosmicpve.reward.nested;

import com.cosmicpve.reward.animation.LootAnimationPreviewProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** A row is the sampling identity; generated item identity is deliberately separate. */
public final class WeightedNestedRewards {
    public record Row(String id, int weight, Function<RandomSource, ItemStack> factory, ItemStack preview) {
        public Row {
            if (id == null || id.isBlank() || weight <= 0 || factory == null || preview.isEmpty())
                throw new IllegalArgumentException("Invalid nested reward row");
            preview = preview.copy();
        }
        public ItemStack create(RandomSource random) { return factory.apply(random); }
        @Override public ItemStack preview() { return preview.copy(); }
    }

    private WeightedNestedRewards() {}

    public static List<Row> selectRows(List<Row> catalog, int count, boolean withoutReplacement, RandomSource random) {
        if (catalog.isEmpty() || count < 1 || (withoutReplacement && count > catalog.size()))
            throw new IllegalArgumentException("Invalid nested reward draw");
        var eligible = new ArrayList<>(catalog);
        var selected = new ArrayList<Row>(count);
        for (int draw = 0; draw < count; draw++) {
            int total = eligible.stream().mapToInt(Row::weight).sum();
            int roll = random.nextInt(total);
            for (int index = 0; index < eligible.size(); index++) {
                Row row = eligible.get(index);
                roll -= row.weight();
                if (roll < 0) {
                    selected.add(row);
                    if (withoutReplacement) eligible.remove(index);
                    break;
                }
            }
        }
        return List.copyOf(selected);
    }

    public static List<ItemStack> create(List<Row> selected, RandomSource random) {
        return selected.stream().map(row -> row.create(random)).toList();
    }

    public static LootAnimationPreviewProvider cosmeticFrames(List<Row> catalog) {
        return LootAnimationPreviewProvider.weighted(catalog.stream()
                .map(row -> new LootAnimationPreviewProvider.WeightedPreview(row.preview(), row.weight())).toList());
    }

    /** Preview lists one payload family at its maximum quantity; opening still selects original rows. */
    public static List<ItemStack> previewMaximums(List<Row> catalog) {
        var visible = new ArrayList<ItemStack>();
        for (Row row : catalog) {
            ItemStack preview = row.preview();
            int match = -1;
            for (int index = 0; index < visible.size(); index++) {
                if (ItemStack.isSameItemSameComponents(visible.get(index), preview)) {
                    match = index;
                    break;
                }
            }
            if (match < 0) visible.add(preview);
            else if (preview.getCount() > visible.get(match).getCount()) visible.set(match, preview);
        }
        return List.copyOf(visible);
    }
}
