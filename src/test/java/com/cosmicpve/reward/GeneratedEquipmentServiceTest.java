package com.cosmicpve.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.content.definition.reward.EnchantmentLevelMode;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentCategory;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentDefinition;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.registry.ModEnchantments;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class GeneratedEquipmentServiceTest {
    private static final Set<net.minecraft.world.item.Item> IRON = Set.of(
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);

    @Test void futureSpaceChestPatternsProduceDistinctCompatibleCapacityBoundEnchantments() {
        var registry = registry();
        var service = new GeneratedEquipmentService();
        var patterns = List.of(
                new GeneratedEquipmentDefinition(GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE,
                        1, 2, CosmicEnchantmentTier.ULTIMATE, EnchantmentLevelMode.MAXIMUM),
                new GeneratedEquipmentDefinition(GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE,
                        1, 2, CosmicEnchantmentTier.LEGENDARY, EnchantmentLevelMode.RANDOM_VALID),
                new GeneratedEquipmentDefinition(GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE,
                        2, 3, CosmicEnchantmentTier.LEGENDARY, EnchantmentLevelMode.RANDOM_VALID));
        for (int pattern = 0; pattern < patterns.size(); pattern++) {
            var definition = patterns.get(pattern);
            for (int seed = 0; seed < 30; seed++) {
                var stack = service.generate(definition, registry, RandomSource.create(seed));
                assertTrue(IRON.contains(stack.getItem()));
                var applied = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                assertTrue(applied.size() >= definition.minimumEnchantments());
                assertTrue(applied.size() <= definition.maximumEnchantments());
                assertTrue(applied.size() <= 5);
                applied.entrySet().forEach(entry -> {
                    var id = entry.getKey().unwrapKey().orElseThrow().identifier();
                    var spec = CosmicEnchantmentSpecs.find(id).orElseThrow();
                    assertTrue(spec.tier().ordinal() <= definition.maximumRarity().ordinal());
                    assertTrue(entry.getIntValue() >= 1 && entry.getIntValue() <= spec.maxLevel());
                    if (definition.levelMode() == EnchantmentLevelMode.MAXIMUM)
                        assertEquals(spec.maxLevel(), entry.getIntValue());
                });
            }
        }
    }

    private static net.minecraft.core.Registry<Enchantment> registry() {
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        register(registry, ModEnchantments.ANGELIC, 5);
        register(registry, ModEnchantments.MOLTEN, 4);
        register(registry, ModEnchantments.ARMORED, 4);
        register(registry, ModEnchantments.ENDER_SHIFT, 3);
        register(registry, ModEnchantments.GLOWING, 1);
        register(registry, ModEnchantments.NUTRITION, 3);
        return registry.freeze();
    }

    private static void register(MappedRegistry<Enchantment> registry,
            net.minecraft.resources.ResourceKey<Enchantment> key, int maxLevel) {
        var supported = HolderSet.direct(List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS, Items.IRON_BOOTS).stream().map(BuiltInRegistries.ITEM::wrapAsHolder).toList());
        var definition = Enchantment.definition(supported, 1, maxLevel,
                Enchantment.constantCost(1), Enchantment.constantCost(1), 1, EquipmentSlotGroup.ARMOR);
        registry.register(key, new Enchantment(Component.literal(key.identifier().toString()), definition,
                HolderSet.empty(), DataComponentMap.EMPTY), RegistrationInfo.BUILT_IN);
    }
}
