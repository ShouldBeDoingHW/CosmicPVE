package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.BlackScrollData;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Atomic extraction mutation. Returned output is placed on the consumed cursor by the event bridge. */
public final class BlackScrollExtractionService {
    private static final Identifier INVALID_ID = CosmicPVE.id("invalid");

    private final Registry<Enchantment> enchantments;
    private final IntUnaryOperator candidateIndex;
    private final IntSupplier destroyRate;

    public BlackScrollExtractionService(
            Registry<Enchantment> enchantments, IntUnaryOperator candidateIndex, IntSupplier destroyRate) {
        this.enchantments = enchantments;
        this.candidateIndex = candidateIndex;
        this.destroyRate = destroyRate;
    }

    public BlackScrollExtractionResult apply(ItemStack scroll, ItemStack expectedTarget, ItemStack currentTarget) {
        if (expectedTarget != currentTarget) return rejected(BlackScrollExtractionResult.Outcome.STALE_TARGET);
        var data = scroll.get(ModDataComponents.BLACK_SCROLL.get());
        if (!scroll.is(ModItems.BLACK_SCROLL.get()) || data == null
                || data.returnedSuccessRate() < 1 || data.returnedSuccessRate() > 100) {
            return rejected(BlackScrollExtractionResult.Outcome.REJECTED_INVALID_SCROLL);
        }
        if (!EquipmentInteractionPolicy.isPotentialEquipment(currentTarget)) {
            return rejected(BlackScrollExtractionResult.Outcome.REJECTED_TARGET);
        }

        var candidates = eligibleActualEnchantments(currentTarget);
        if (candidates.isEmpty()) {
            return new BlackScrollExtractionResult(
                    BlackScrollExtractionResult.Outcome.REJECTED_NO_ELIGIBLE_ENCHANTMENTS,
                    INVALID_ID, 0, 0, 0, ItemStack.EMPTY, 0);
        }
        int selectedIndex = candidates.size() == 1 ? 0 : candidateIndex.applyAsInt(candidates.size());
        if (selectedIndex < 0 || selectedIndex >= candidates.size()) {
            throw new IllegalStateException("Black Scroll candidate index must be within the candidate list");
        }
        int rolledDestroyRate = destroyRate.getAsInt();
        if (rolledDestroyRate < 1 || rolledDestroyRate > 100) {
            throw new IllegalStateException("Black Scroll returned Destroy Rate must be in [1,100]");
        }

        var selected = candidates.get(selectedIndex);
        var output = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        output.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION, selected.id(), selected.level(),
                data.returnedSuccessRate(), rolledDestroyRate));

        EnchantmentHelper.updateEnchantments(currentTarget,
                mutable -> mutable.removeIf(holder -> holder.equals(selected.holder())));
        scroll.shrink(1);
        return new BlackScrollExtractionResult(
                BlackScrollExtractionResult.Outcome.SUCCESS, selected.id(), selected.level(),
                data.returnedSuccessRate(), rolledDestroyRate, output, candidates.size());
    }

    public java.util.List<ExtractableEnchantment> eligibleActualEnchantments(ItemStack target) {
        var candidates = new ArrayList<ExtractableEnchantment>();
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(target).entrySet()) {
            var key = entry.getKey().unwrapKey();
            if (key.isEmpty()) continue;
            Identifier id = key.orElseThrow().identifier();
            var spec = CosmicEnchantmentSpecs.find(id);
            var registered = enchantments.get(id);
            if (spec.isPresent() && spec.orElseThrow().tier().extractableByBlackScroll()
                    && registered.isPresent() && registered.orElseThrow().equals(entry.getKey())) {
                candidates.add(new ExtractableEnchantment(id, entry.getIntValue(), entry.getKey()));
            }
        }
        candidates.sort(Comparator.comparing(candidate -> candidate.id().toString()));
        return java.util.List.copyOf(candidates);
    }

    private static BlackScrollExtractionResult rejected(BlackScrollExtractionResult.Outcome outcome) {
        return new BlackScrollExtractionResult(outcome, INVALID_ID, 0, 0, 0, ItemStack.EMPTY, 0);
    }

    public record ExtractableEnchantment(Identifier id, int level, Holder<Enchantment> holder) {}
}
