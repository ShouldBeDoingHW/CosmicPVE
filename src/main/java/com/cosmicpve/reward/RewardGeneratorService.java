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
            case RewardDescriptor.PinpointBook ignored -> Optional.of(
                    com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.pinpointBook(context.random()));
            case RewardDescriptor.RandomRangerArmor ignored -> Optional.of(
                    com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.randomRangerArmor(context.registries(),context.random()));
            case RewardDescriptor.AdvancedBanknote ignored -> Optional.of(
                    com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.banknote(context.random()));
            case RewardDescriptor.SkipTwoPortal ignored -> Optional.of(
                    com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.portal(2,0));
            case RewardDescriptor.MadnessThreePortal ignored -> Optional.of(
                    com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.portal(0,3));
            case RewardDescriptor.MemoryChest ignored -> Optional.of(
                    new ItemStack(com.cosmicpve.registry.ModItems.MEMORY_CHEST.get()));
            case RewardDescriptor.RandomTrialTrinket reward -> Optional.of(
                    com.cosmicpve.trial.trinket.TrialTrinkets.randomTier(reward.tier(), context.random()));
            case RewardDescriptor.AccessorySocket reward -> {
                var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(reward.itemId()));
                if (stack.is(com.cosmicpve.registry.ModItems.OMNI_SOCKET.get()))
                    stack.set(com.cosmicpve.registry.ModDataComponents.OMNI_SOCKET_SUCCESS.get(), reward.successRate());
                else {
                    var slot = stack.is(com.cosmicpve.registry.ModItems.AMULET_SOCKET.get())
                            ? com.cosmicpve.data.component.AccessorySlot.AMULET : com.cosmicpve.data.component.AccessorySlot.BELT;
                    stack.set(com.cosmicpve.registry.ModDataComponents.ACCESSORY_SOCKET.get(),
                            new com.cosmicpve.data.component.AccessorySocketData(
                                    com.cosmicpve.data.component.AccessorySocketData.DATA_VERSION, slot, reward.successRate()));
                }
                yield Optional.of(stack);
            }
            case RewardDescriptor.TrialPortalPreset reward -> {
                try { yield Optional.of(com.cosmicpve.trial.portal.TrialPortalPresets.generate(reward.presetId())); }
                catch (IllegalArgumentException unavailable) {
                    com.cosmicpve.CosmicPVE.LOGGER.warn("Deferred Trial Portal reward: {}", unavailable.getMessage());
                    yield Optional.empty();
                }
            }
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
