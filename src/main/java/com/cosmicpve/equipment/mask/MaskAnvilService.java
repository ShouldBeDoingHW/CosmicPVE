package com.cosmicpve.equipment.mask;

import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Pure validation/construction seam for zero-cost vanilla-anvil Multi-Mask creation. */
public final class MaskAnvilService {
    public enum Outcome { SUCCESS, NOT_MASKS, INVALID, DUPLICATE, TOO_MANY }
    public record Result(Outcome outcome, ItemStack output, List<net.minecraft.resources.Identifier> maskIds) {
        public Result { output = output.copy(); maskIds = List.copyOf(maskIds); }
    }

    public Result combine(ItemStack left, ItemStack right, int limit, CosmicContentRepository content) {
        if (!left.is(ModItems.MASK.get()) || !right.is(ModItems.MASK.get()))
            return new Result(Outcome.NOT_MASKS, ItemStack.EMPTY, List.of());
        var leftMasks = left.get(ModDataComponents.MASK_ITEM.get());
        var rightMasks = right.get(ModDataComponents.MASK_ITEM.get());
        if (leftMasks == null || rightMasks == null || !leftMasks.valid() || !rightMasks.valid())
            return new Result(Outcome.INVALID, ItemStack.EMPTY, List.of());
        var ids = new ArrayList<>(leftMasks.maskIds());
        ids.addAll(rightMasks.maskIds());
        if (new HashSet<>(ids).size() != ids.size())
            return new Result(Outcome.DUPLICATE, ItemStack.EMPTY, List.of());
        if (ids.size() > limit)
            return new Result(Outcome.TOO_MANY, ItemStack.EMPTY, List.of());
        try { return new Result(Outcome.SUCCESS, MaskItemFactory.create(ids, content), ids); }
        catch (IllegalArgumentException exception) {
            return new Result(Outcome.INVALID, ItemStack.EMPTY, List.of());
        }
    }
}
