package com.cosmicpve.equipment.enchantment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** One source of truth for actual-Cosmic ordering seen by Transmog and selection menus. */
public final class VisibleCosmicEnchantmentOrdering {
    public record Entry(Identifier id, int level, Holder<Enchantment> holder, CosmicEnchantmentSpec spec, int originalIndex) {}
    private VisibleCosmicEnchantmentOrdering() {}

    public static List<Entry> eligibleBlackScroll(ItemStack stack) {
        var entries = new ArrayList<Entry>();
        int index = 0;
        for (var value : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            var key = value.getKey().unwrapKey();
            if (key.isPresent()) {
                var spec = CosmicEnchantmentSpecs.find(key.orElseThrow().identifier());
                if (spec.isPresent() && spec.orElseThrow().tier().extractableByBlackScroll()) {
                    entries.add(new Entry(key.orElseThrow().identifier(), value.getIntValue(), value.getKey(),
                            spec.orElseThrow(), index));
                }
            }
            index++;
        }
        if (new TransmogApplicationService().isApplied(stack)) entries.sort((left, right) ->
                TransmogTooltipOrdering.COMPARATOR.compare(key(left), key(right)));
        return List.copyOf(entries);
    }

    private static TransmogTooltipOrdering.Key key(Entry entry) {
        return new TransmogTooltipOrdering.Key(true, entry.spec().tier(), entry.level(), entry.id(), entry.originalIndex());
    }
}
