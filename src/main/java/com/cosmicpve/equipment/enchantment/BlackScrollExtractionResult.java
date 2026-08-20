package com.cosmicpve.equipment.enchantment;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record BlackScrollExtractionResult(
        Outcome outcome, Identifier enchantmentId, int level, int returnedSuccessRate,
        int returnedDestroyRate, ItemStack returnedBook, int eligibleCandidateCount) {
    public enum Outcome {
        SUCCESS, REJECTED_INVALID_SCROLL, REJECTED_TARGET, REJECTED_NO_ELIGIBLE_ENCHANTMENTS, STALE_TARGET
    }

    public boolean succeeded() { return outcome == Outcome.SUCCESS; }
    public boolean consumedScroll() { return succeeded(); }
}
