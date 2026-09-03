package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.OptionalInt;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class CosmicBookApplicationService {
    private final Registry<Enchantment> enchantments;
    private final CustomEnchantCapacityService capacity;
    private final WhiteScrollProtectionService protection;
    private final IntSupplier roll;
    private final IntUnaryOperator destroyRateModifier;
    public CosmicBookApplicationService(Registry<Enchantment> enchantments, CustomEnchantCapacityService capacity,
            WhiteScrollProtectionService protection, IntSupplier roll) {
        this(enchantments, capacity, protection, roll, IntUnaryOperator.identity());
    }
    public CosmicBookApplicationService(Registry<Enchantment> enchantments, CustomEnchantCapacityService capacity,
            WhiteScrollProtectionService protection, IntSupplier roll, IntUnaryOperator destroyRateModifier) {
        this.enchantments = enchantments; this.capacity = capacity; this.protection = protection; this.roll = roll;
        this.destroyRateModifier = destroyRateModifier;
    }

    public CosmicBookApplicationResult apply(ItemStack book, ItemStack expected, ItemStack target) {
        var data = book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        var id = data == null ? com.cosmicpve.CosmicPVE.id("invalid") : data.enchantmentId();
        int used = capacity.used(target), limit = capacity.capacity(target);
        boolean protectedBefore = protection.isProtected(target);
        if (expected != target) return result(CosmicBookApplicationResult.Outcome.STALE_TARGET, id, data, 0, used, limit, protectedBefore);
        if (!book.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get()) || data == null) return result(CosmicBookApplicationResult.Outcome.REJECTED_INVALID_BOOK, id, data, 0, used, limit, protectedBefore);
        var spec = CosmicEnchantmentSpecs.find(id);
        var holder = enchantments.get(id);
        if (spec.isEmpty() || holder.isEmpty() || !CosmicBookRateRules.allows(book, spec.orElseThrow(), data))
            return result(CosmicBookApplicationResult.Outcome.REJECTED_INVALID_BOOK, id, data, 0, used, limit, protectedBefore);
        var enchantment = holder.orElseThrow();
        if (!enchantment.value().canEnchant(target)) return result(CosmicBookApplicationResult.Outcome.REJECTED_TARGET, id, data, 0, used, limit, protectedBefore);
        if (data.level() < 1 || data.level() > enchantment.value().getMaxLevel())
            return result(CosmicBookApplicationResult.Outcome.REJECTED_LEVEL, id, data, 0, used, limit, protectedBefore);
        var currentEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(target);
        int existing = currentEnchantments.getLevel(enchantment);
        var ordinaryId = HeroicEnchantments.ordinaryFor(id);
        var heroicId = HeroicEnchantments.heroicFor(id);
        if (heroicId.isPresent()) {
            var heroicHolder = enchantments.get(heroicId.orElseThrow());
            if (heroicHolder.isPresent() && currentEnchantments.getLevel(heroicHolder.orElseThrow()) > 0)
                return result(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_COUNTERPART, id, data, 0, used, limit, protectedBefore);
        }
        net.minecraft.core.Holder.Reference<Enchantment> ordinaryHolder = null;
        boolean initialHeroicConversion = false;
        if (ordinaryId.isPresent() && existing == 0) {
            ordinaryHolder = enchantments.get(ordinaryId.orElseThrow()).orElse(null);
            var ordinarySpec = CosmicEnchantmentSpecs.find(ordinaryId.orElseThrow()).orElse(null);
            int ordinaryLevel = ordinaryHolder == null ? 0 : currentEnchantments.getLevel(ordinaryHolder);
            if (ordinarySpec == null || ordinaryLevel != ordinarySpec.maxLevel())
                return result(CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE, id, data, 0, used, limit, protectedBefore);
            initialHeroicConversion = true;
        }
        int applied = data.level() == existing ? existing + 1 : data.level();
        if (data.level() < existing || applied > enchantment.value().getMaxLevel())
            return result(CosmicBookApplicationResult.Outcome.REJECTED_EXISTING_LEVEL, id, data, applied, used, limit, protectedBefore);
        if (!initialHeroicConversion && !capacity.canAdd(target, id)) return result(CosmicBookApplicationResult.Outcome.REJECTED_CAPACITY, id, data, applied, used, limit, protectedBefore);

        int effectiveDestroyRate = Math.max(0, Math.min(100, destroyRateModifier.applyAsInt(data.destroyRate())));
        var decision = CosmicBookRollResolver.resolve(data.successRate(), effectiveDestroyRate, roll);
        int successRoll = decision.successRoll();
        book.shrink(1);
        if (decision.outcome() == CosmicBookRollResolver.Outcome.SUCCESS) {
            var removeOrdinary = ordinaryHolder;
            EnchantmentHelper.updateEnchantments(target, mutable -> {
                if (removeOrdinary != null) mutable.removeIf(candidate -> candidate.equals(removeOrdinary));
                mutable.set(enchantment, applied);
            });
            return result(CosmicBookApplicationResult.Outcome.SUCCESS, id, data, applied, used, limit, protectedBefore, successRoll, null);
        }
        int destroyRoll = decision.destroyRoll().orElseThrow();
        if (decision.outcome() == CosmicBookRollResolver.Outcome.FAILED_SURVIVED)
            return result(CosmicBookApplicationResult.Outcome.FAILED_SURVIVED, id, data, 0, used, limit, protectedBefore, successRoll, destroyRoll);
        if (protection.consumeIfProtected(target))
            return result(CosmicBookApplicationResult.Outcome.FAILED_PROTECTED, id, data, 0, used, limit, protectedBefore, successRoll, destroyRoll);
        target.setCount(0);
        return result(CosmicBookApplicationResult.Outcome.FAILED_DESTROYED, id, data, 0, used, limit, protectedBefore, successRoll, destroyRoll);
    }
    private CosmicBookApplicationResult result(CosmicBookApplicationResult.Outcome out, net.minecraft.resources.Identifier id,
            CosmicEnchantmentBookData data, int applied, int used, int limit, boolean protectedBefore) {
        return result(out,id,data,applied,used,limit,protectedBefore,null,null); }
    private CosmicBookApplicationResult result(CosmicBookApplicationResult.Outcome out, net.minecraft.resources.Identifier id,
            CosmicEnchantmentBookData data, int applied, int used, int limit, boolean protectedBefore, Integer sr, Integer dr) {
        return new CosmicBookApplicationResult(out,id,data==null?0:data.level(),applied,data==null?0:data.successRate(),
                sr==null?OptionalInt.empty():OptionalInt.of(sr),data==null?0:data.destroyRate(),
                dr==null?OptionalInt.empty():OptionalInt.of(dr),used,limit,protectedBefore);
    }
}
