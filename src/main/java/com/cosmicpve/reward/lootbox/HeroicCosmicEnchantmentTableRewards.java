package com.cosmicpve.reward.lootbox;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.HeroicEnchantments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class HeroicCosmicEnchantmentTableRewards {
    public static final List<Identifier> POOL = HeroicEnchantments.PAIRS.stream().map(HeroicEnchantments.Pair::heroic).toList();
    public static final List<Integer> SUCCESS = List.of(25, 50, 75);
    public static final int ENTRY_COUNT = 30;

    public ItemStack create(Registry<Enchantment> registry, RandomSource random) {
        return create(registry, POOL.get(random.nextInt(POOL.size())),
                SUCCESS.get(random.nextInt(SUCCESS.size())), random);
    }
    public ItemStack create(Registry<Enchantment> registry, Identifier id, int success, RandomSource random) {
        if (!POOL.contains(id) || !SUCCESS.contains(success)) throw new IllegalArgumentException("Invalid Heroic Table entry");
        var spec = CosmicEnchantmentSpecs.find(id).orElseThrow();
        var holder = registry.get(id).orElseThrow();
        ItemStack stack = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new CosmicEnchantmentBookData(
                CosmicEnchantmentBookData.CURRENT_DATA_VERSION, id, spec.maxLevel(), success,
                random.nextIntBetweenInclusive(1, 100)));
        return stack;
    }
}
