package com.cosmicpve.content.definition.reward;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import net.minecraft.resources.Identifier;

public sealed interface RewardDescriptor permits RewardDescriptor.StaticItem, RewardDescriptor.Banknote,
        RewardDescriptor.CosmicBook, RewardDescriptor.UnexaminedBook, RewardDescriptor.BlackScroll, RewardDescriptor.ArmorOrb,
        RewardDescriptor.WeaponOrb, RewardDescriptor.MobSpawner, RewardDescriptor.GeneratedEquipment,
        RewardDescriptor.SpaceChest, RewardDescriptor.Mask, RewardDescriptor.ArmorSetCrystal,
        RewardDescriptor.XpBottle, RewardDescriptor.RandomVKitCrystal, RewardDescriptor.EnchantedBlackScroll,
        RewardDescriptor.TrialPortalPreset, RewardDescriptor.RandomTrialTrinket, RewardDescriptor.AccessorySocket,
        RewardDescriptor.PinpointBook, RewardDescriptor.RandomRangerArmor, RewardDescriptor.AdvancedBanknote,
        RewardDescriptor.SkipTwoPortal, RewardDescriptor.MadnessThreePortal, RewardDescriptor.MemoryChest {
    record PinpointBook() implements RewardDescriptor { public RewardType type(){return RewardType.PINPOINT_BOOK;} }
    record RandomRangerArmor() implements RewardDescriptor { public RewardType type(){return RewardType.RANDOM_RANGER_ARMOR;} }
    record AdvancedBanknote() implements RewardDescriptor { public RewardType type(){return RewardType.ADVANCED_BANKNOTE;} }
    record SkipTwoPortal() implements RewardDescriptor { public RewardType type(){return RewardType.SKIP_TWO_PORTAL;} }
    record MadnessThreePortal() implements RewardDescriptor { public RewardType type(){return RewardType.MADNESS_THREE_PORTAL;} }
    record MemoryChest() implements RewardDescriptor { public RewardType type(){return RewardType.MEMORY_CHEST;} }
    record RandomTrialTrinket(int tier) implements RewardDescriptor {
        public RandomTrialTrinket { if (tier < 1 || tier > 3) throw new IllegalArgumentException("trinket_tier must be in [1,3]"); }
        public RewardType type() { return RewardType.RANDOM_TRIAL_TRINKET; }
    }
    record AccessorySocket(Identifier itemId, int successRate) implements RewardDescriptor {
        public AccessorySocket {
            if (successRate < 1 || successRate > 100) throw new IllegalArgumentException("success_rate must be in [1,100]");
            if (!java.util.List.of(Identifier.parse("cosmicpve:amulet_socket"), Identifier.parse("cosmicpve:belt_socket"),
                    Identifier.parse("cosmicpve:omni_socket")).contains(itemId))
                throw new IllegalArgumentException("Unknown accessory socket item");
        }
        public RewardType type() { return RewardType.ACCESSORY_SOCKET; }
    }
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
