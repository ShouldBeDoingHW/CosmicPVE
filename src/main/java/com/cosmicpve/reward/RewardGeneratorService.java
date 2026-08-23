package com.cosmicpve.reward;

import com.cosmicpve.content.definition.reward.RewardDescriptor;
import com.cosmicpve.economy.Banknotes;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.EnchantingRewardItemFactory;
import com.cosmicpve.equipment.enchantment.OrbType;
import com.cosmicpve.equipment.enchantment.UnexaminedBooks;
import com.cosmicpve.reward.spawner.MobSpawners;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;

public final class RewardGeneratorService {
    private final EnchantingRewardItemFactory enchanting = new EnchantingRewardItemFactory();
    private final GeneratedEquipmentService equipment = new GeneratedEquipmentService();

    public Optional<ItemStack> generate(RewardDescriptor descriptor, RewardGenerationContext context) {
        return switch (descriptor) {
            case RewardDescriptor.StaticItem reward -> BuiltInRegistries.ITEM.get(reward.itemId())
                    .map(holder -> new ItemStack(holder.value()));
            case RewardDescriptor.Banknote reward -> Optional.of(Banknotes.create(reward.cents()));
            case RewardDescriptor.CosmicBook reward -> UnexaminedBooks.openingService()
                    .roll(reward.rarity(), CosmicEnchantmentSpecs.ALL, context.random(), context.player())
                    .map(UnexaminedBooks::revealed);
            case RewardDescriptor.BlackScroll reward -> Optional.of(enchanting.blackScroll(reward.successRate()));
            case RewardDescriptor.ArmorOrb reward -> Optional.of(
                    enchanting.orb(OrbType.ARMOR, reward.successRate(), context.random()));
            case RewardDescriptor.WeaponOrb reward -> Optional.of(
                    enchanting.orb(OrbType.WEAPON, reward.successRate(), context.random()));
            case RewardDescriptor.MobSpawner reward -> Optional.of(MobSpawners.create(reward.entityTypeId(), 1));
            case RewardDescriptor.GeneratedEquipment reward -> Optional.of(equipment.generate(
                    reward.definition(), context.registries().lookupOrThrow(Registries.ENCHANTMENT), context.random()));
        };
    }
}
