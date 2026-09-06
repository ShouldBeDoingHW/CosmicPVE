package com.cosmicpve.reward;

import com.cosmicpve.content.definition.reward.EnchantmentLevelMode;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentDefinition;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class GeneratedEquipmentService {
    private static final List<Item> IRON_ARMOR = List.of(
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
    private final CustomEnchantCapacityService capacity = new CustomEnchantCapacityService();

    public ItemStack generate(GeneratedEquipmentDefinition definition, Registry<Enchantment> enchantments,
            RandomSource random) {
        ItemStack result = new ItemStack(IRON_ARMOR.get(random.nextInt(IRON_ARMOR.size())));
        List<CosmicEnchantmentSpec> candidates = candidates(result, definition, enchantments);
        shuffle(candidates, random);
        int requested = random.nextIntBetweenInclusive(
                definition.minimumEnchantments(), definition.limitToAvailable() ? Math.min(definition.maximumEnchantments(), candidates.size()) : definition.maximumEnchantments());
        if (requested > candidates.size() || requested > capacity.capacity(result)) {
            throw new IllegalStateException("Validated generated-equipment definition cannot produce " + requested
                    + " distinct enchantments for " + result.getItem());
        }
        int count = requested;
        EnchantmentHelper.updateEnchantments(result, mutable -> {
            for (int index = 0; index < count; index++) {
                CosmicEnchantmentSpec spec = candidates.get(index);
                int level = definition.levelMode() == EnchantmentLevelMode.MAXIMUM
                        ? spec.maxLevel() : random.nextIntBetweenInclusive(1, spec.maxLevel());
                mutable.set(enchantments.get(spec.id()).orElseThrow(), level);
            }
        });
        return result;
    }

    public List<CosmicEnchantmentSpec> candidates(ItemStack stack, GeneratedEquipmentDefinition definition,
            Registry<Enchantment> enchantments) {
        return new ArrayList<>(CosmicEnchantmentSpecs.ALL.stream()
                .filter(spec -> spec.tier().ordinal() <= definition.maximumRarity().ordinal())
                .filter(spec -> enchantments.get(spec.id())
                        .filter(holder -> holder.value().canEnchant(stack)).isPresent())
                .toList());
    }

    private static <T> void shuffle(List<T> values, RandomSource random) {
        for (int index = values.size() - 1; index > 0; index--) {
            int replacement = random.nextInt(index + 1);
            T value = values.get(index);
            values.set(index, values.get(replacement));
            values.set(replacement, value);
        }
    }
}
