package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;

/** Typed Holy state and the deterministic White Scroll -> Holy conversion transaction. */
public final class HolyWhiteScrollService {
    public enum Outcome { SUCCESS, REJECTED_STALE, REJECTED_TARGET, REJECTED_UNPROTECTED, REJECTED_ALREADY_HOLY }

    private final WhiteScrollProtectionService whiteScrolls = new WhiteScrollProtectionService();

    public Outcome apply(ItemStack scroll, ItemStack targetSnapshot, ItemStack target) {
        if (targetSnapshot != target) return Outcome.REJECTED_STALE;
        if (!scroll.is(ModItems.HOLY_WHITE_SCROLL.get()) || scroll.isEmpty() || !eligible(target)) return Outcome.REJECTED_TARGET;
        if (isHoly(target)) return Outcome.REJECTED_ALREADY_HOLY;
        if (!whiteScrolls.isProtected(target)) return Outcome.REJECTED_UNPROTECTED;
        whiteScrolls.consumeIfProtected(target);
        target.set(ModDataComponents.HOLY.get(), true);
        scroll.shrink(1);
        return Outcome.SUCCESS;
    }

    public static boolean isHoly(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModDataComponents.HOLY.get()));
    }

    public static void consumeHoly(ItemStack stack) {
        stack.remove(ModDataComponents.HOLY.get());
    }

    public static boolean eligible(ItemStack stack) {
        if (stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)) return true;
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)
                || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem
                || stack.is(ItemTags.PICKAXES)) return true;
        // Unit/bootstrap contexts do not always bind vanilla tags; registry identity remains authoritative.
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings")
                || path.endsWith("_boots") || path.endsWith("_sword") || path.endsWith("_axe")
                || path.endsWith("_pickaxe") || path.equals("bow") || path.equals("crossbow");
    }

    public static double preservationChance(boolean monopolyActive) { return monopolyActive ? .55 : .50; }
}
