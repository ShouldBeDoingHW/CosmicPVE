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
            case RewardDescriptor.UnexaminedBook reward -> Optional.of(UnexaminedBooks.create(reward.rarity()));
            case RewardDescriptor.BlackScroll reward -> Optional.of(enchanting.blackScroll(reward.successRate()));
            case RewardDescriptor.ArmorOrb reward -> Optional.of(reward.successRate() == 0
                    ? enchanting.randomOrb(OrbType.ARMOR, context.random())
                    : enchanting.orb(OrbType.ARMOR, reward.successRate(), context.random()));
            case RewardDescriptor.WeaponOrb reward -> Optional.of(reward.successRate() == 0
                    ? enchanting.randomOrb(OrbType.WEAPON, context.random())
                    : enchanting.orb(OrbType.WEAPON, reward.successRate(), context.random()));
            case RewardDescriptor.MobSpawner reward -> Optional.of(MobSpawners.create(reward.entityTypeId(), 1));
            case RewardDescriptor.GeneratedEquipment reward -> Optional.of(equipment.generate(
                    reward.definition(), context.registries().lookupOrThrow(Registries.ENCHANTMENT), context.random()));
            case RewardDescriptor.SpaceChest reward -> Optional.of(com.cosmicpve.spacechest.SpaceChests.create(reward.tier()));
            case RewardDescriptor.Mask reward -> {
                var ids = new java.util.ArrayList<>(com.cosmicpve.content.CosmicContent.repository()
                        .snapshot().maskDefinitions().keySet());
                if (ids.size() < reward.maskCount()) yield Optional.empty();
                for (int i = ids.size() - 1; i > 0; i--) {
                    int j = context.random().nextInt(i + 1);
                    var swap = ids.get(i); ids.set(i, ids.get(j)); ids.set(j, swap);
                }
                yield Optional.of(com.cosmicpve.equipment.mask.MaskItemFactory.create(
                        ids.subList(0, reward.maskCount())));
            }
            case RewardDescriptor.ArmorSetCrystal reward -> com.cosmicpve.content.CosmicContent.repository()
                    .findArmorSetDefinition(reward.armorSetId())
                    .map(definition -> com.cosmicpve.equipment.armor.ArmorSetCrystals.create(definition, reward.successRate()));
            case RewardDescriptor.XpBottle reward -> Optional.of(
                    new com.cosmicpve.tinkerer.GearSalvageService().bottle(reward.experience()));
            case RewardDescriptor.RandomVKitCrystal ignored -> {
                var definitions = com.cosmicpve.vkit.VKitDefinition.ALL;
                var definition = definitions.get(context.random().nextInt(definitions.size()));
                yield Optional.of(new net.minecraft.world.item.ItemStack(switch (definition.id().getPath()) {
                    case "phoenix" -> com.cosmicpve.registry.ModItems.PHOENIX_VKIT_CRYSTAL.get();
                    case "ogre" -> com.cosmicpve.registry.ModItems.OGRE_VKIT_CRYSTAL.get();
                    case "judgement" -> com.cosmicpve.registry.ModItems.JUDGEMENT_VKIT_CRYSTAL.get();
                    case "slayer" -> com.cosmicpve.registry.ModItems.SLAYER_VKIT_CRYSTAL.get();
                    default -> throw new IllegalStateException("Unknown canonical V-Kit: " + definition.id());
                }));
            }
            case RewardDescriptor.EnchantedBlackScroll reward -> Optional.of(
                    enchanting.enchantedBlackScroll(reward.successRate()));
        };
    }
}
