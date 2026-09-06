package com.cosmicpve.reward.lootbox;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CosmicBookRateOverride;
import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class CosmicEnchantmentTableRewards {
    public static final Identifier SOURCE_ID = CosmicPVE.id("cosmic_enchantment_table");
    /** Code-level mirror of the Design Doc's canonical starred ordinary-enchantment membership. */
    public static final List<ResourceKey<Enchantment>> POOL = List.of(
            ModEnchantments.ANGELIC, ModEnchantments.ARMORED, ModEnchantments.EAGLE_EYE,
            ModEnchantments.LIGHTNING, ModEnchantments.LUCK, ModEnchantments.MOLTEN,
            ModEnchantments.RAGE, ModEnchantments.VENOM, ModEnchantments.VIRUS,
            ModEnchantments.UNDEAD_RUSE, ModEnchantments.SNIPER, ModEnchantments.OBLITERATE,
            ModEnchantments.LEADERSHIP, ModEnchantments.SOUL_SIPHON, ModEnchantments.STORMCALLER,
            ModEnchantments.DOMINATE, ModEnchantments.SPIRIT_LINK, ModEnchantments.SOLITUDE);
    public static final List<Integer> ORDINARY_SUCCESS = List.of(50, 75, 100);
    public static final List<Integer> MASTERY_SUCCESS = List.of(25, 50);

    public ItemStack create(Registry<Enchantment> registry, RandomSource random) {
        var key = POOL.get(random.nextInt(POOL.size()));
        boolean mastery = CosmicEnchantmentSpecs.find(key.identifier()).orElseThrow().tier() == CosmicEnchantmentTier.MASTERY;
        var rates = mastery ? MASTERY_SUCCESS : ORDINARY_SUCCESS;
        return create(registry, key, rates.get(random.nextInt(rates.size())), random);
    }
    public ItemStack create(Registry<Enchantment> registry, ResourceKey<Enchantment> key, int success, RandomSource random) {
        if (!POOL.contains(key)) throw new IllegalArgumentException("Enchantment is not in the Cosmic Enchantment Table pool");
        var holder = registry.getOrThrow(key);
        var id = key.identifier();
        var spec = CosmicEnchantmentSpecs.find(id).orElseThrow();
        if (holder.value().getMaxLevel() != spec.maxLevel())
            throw new IllegalStateException("Registered/spec max-level mismatch for " + id);
        boolean mastery = spec.tier() == CosmicEnchantmentTier.MASTERY;
        if (!(mastery ? MASTERY_SUCCESS : ORDINARY_SUCCESS).contains(success))
            throw new IllegalArgumentException("Invalid Table Success Rate for " + id + ": " + success);
        int destroy = mastery ? random.nextIntBetweenInclusive(51, 100) : random.nextIntBetweenInclusive(1, 100);
        ItemStack stack = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION, id, holder.value().getMaxLevel(), success, destroy));
        if (mastery) stack.set(ModDataComponents.COSMIC_BOOK_RATE_OVERRIDE.get(), new CosmicBookRateOverride(
                CosmicBookRateOverride.CURRENT_DATA_VERSION, SOURCE_ID));
        return stack;
    }
}
