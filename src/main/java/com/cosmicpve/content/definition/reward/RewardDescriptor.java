package com.cosmicpve.content.definition.reward;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import net.minecraft.resources.Identifier;

public sealed interface RewardDescriptor permits RewardDescriptor.StaticItem, RewardDescriptor.Banknote,
        RewardDescriptor.CosmicBook, RewardDescriptor.UnexaminedBook, RewardDescriptor.BlackScroll, RewardDescriptor.ArmorOrb,
        RewardDescriptor.WeaponOrb, RewardDescriptor.MobSpawner, RewardDescriptor.GeneratedEquipment,
        RewardDescriptor.SpaceChest, RewardDescriptor.Mask, RewardDescriptor.ArmorSetCrystal,
        RewardDescriptor.XpBottle, RewardDescriptor.RandomVKitCrystal, RewardDescriptor.EnchantedBlackScroll,
        RewardDescriptor.TrialPortalPreset {
    RewardType type();
    record TrialPortalPreset(Identifier presetId) implements RewardDescriptor {
        public RewardType type(){return RewardType.TRIAL_PORTAL_PRESET;}
    }
    record StaticItem(Identifier itemId) implements RewardDescriptor { public RewardType type(){return RewardType.STATIC_ITEM;} }
    record Banknote(long cents) implements RewardDescriptor { public RewardType type(){return RewardType.BANKNOTE;} }
    record CosmicBook(CosmicEnchantmentTier rarity) implements RewardDescriptor { public RewardType type(){return RewardType.COSMIC_BOOK;} }
    record UnexaminedBook(CosmicEnchantmentTier rarity) implements RewardDescriptor {
        public RewardType type(){return RewardType.UNEXAMINED_BOOK;}
    }
    record BlackScroll(int successRate) implements RewardDescriptor { public RewardType type(){return RewardType.BLACK_SCROLL;} }
    record ArmorOrb(int successRate) implements RewardDescriptor { public RewardType type(){return RewardType.ARMOR_ORB;} }
    record WeaponOrb(int successRate) implements RewardDescriptor { public RewardType type(){return RewardType.WEAPON_ORB;} }
    record MobSpawner(Identifier entityTypeId) implements RewardDescriptor { public RewardType type(){return RewardType.MOB_SPAWNER;} }
    record GeneratedEquipment(GeneratedEquipmentDefinition definition) implements RewardDescriptor {
        public RewardType type(){return RewardType.GENERATED_EQUIPMENT;}
    }
    record SpaceChest(com.cosmicpve.spacechest.SpaceChestTier tier) implements RewardDescriptor {
        public RewardType type(){return RewardType.SPACE_CHEST;}
    }
    record Mask(int maskCount) implements RewardDescriptor { public RewardType type(){return RewardType.MASK;} }
    record ArmorSetCrystal(Identifier armorSetId, int successRate) implements RewardDescriptor {
        public RewardType type(){return RewardType.ARMOR_SET_CRYSTAL;}
    }
    record XpBottle(long experience) implements RewardDescriptor { public RewardType type(){return RewardType.XP_BOTTLE;} }
    record RandomVKitCrystal() implements RewardDescriptor { public RewardType type(){return RewardType.RANDOM_VKIT_CRYSTAL;} }
    record EnchantedBlackScroll(int successRate) implements RewardDescriptor {
        public RewardType type(){return RewardType.ENCHANTED_BLACK_SCROLL;}
    }
}
