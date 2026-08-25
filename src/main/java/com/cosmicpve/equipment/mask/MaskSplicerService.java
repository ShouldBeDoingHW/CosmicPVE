package com.cosmicpve.equipment.mask;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Atomic one-use disassembly transaction for valid Multi-Masks. */
public final class MaskSplicerService {
    private final com.cosmicpve.content.CosmicContentRepository content;
    public MaskSplicerService() { this(com.cosmicpve.content.CosmicContent.repository()); }
    MaskSplicerService(com.cosmicpve.content.CosmicContentRepository content) { this.content = content; }
    public enum Outcome { SUCCESS, STALE, INVALID, SINGLE_MASK }
    public record Result(Outcome outcome, List<ItemStack> outputs) {
        public Result { outputs = List.copyOf(outputs); }
    }

    public Result split(ItemStack splicer, ItemStack mask, ItemStack currentSplicer, ItemStack currentMask) {
        if (splicer != currentSplicer || mask != currentMask) return new Result(Outcome.STALE, List.of());
        var loadout = mask.get(ModDataComponents.MASK_ITEM.get());
        if (!splicer.is(ModItems.MASK_SPLICER.get()) || !mask.is(ModItems.MASK.get())
                || loadout == null || !loadout.valid()) return new Result(Outcome.INVALID, List.of());
        if (loadout.maskIds().size() < 2) return new Result(Outcome.SINGLE_MASK, List.of());
        List<ItemStack> outputs;
        try { outputs = loadout.maskIds().stream().map(id -> MaskItemFactory.create(List.of(id), content)).toList(); }
        catch (IllegalArgumentException exception) { return new Result(Outcome.INVALID, List.of()); }
        splicer.shrink(1);
        mask.shrink(1);
        return new Result(Outcome.SUCCESS, outputs);
    }
}
