package com.cosmicpve.vkit;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Canonical bounded metadata for the four V-Kits. */
public record VKitDefinition(
        Identifier id,
        String displayName,
        int color,
        String flavorTranslation,
        EquipmentReward armor,
        EquipmentReward weapon) {
    public static final VKitDefinition PHOENIX = new VKitDefinition(
            CosmicPVE.id("phoenix"), "Phoenix", 0x3F0761, "tooltip.cosmicpve.vkit.phoenix",
            reward(Items.IRON_BOOTS, "Sandals of the Phoenix", List.of(
                    CosmicEnchantmentSpecs.ENDER_WALKER, CosmicEnchantmentSpecs.GEARS,
                    CosmicEnchantmentSpecs.DODGE, CosmicEnchantmentSpecs.LUCK, CosmicEnchantmentSpecs.STORMCALLER),
                    CosmicEnchantmentSpecs.PHOENIX),
            reward(Items.DIAMOND_SWORD, "Sikanda", List.of(
                    CosmicEnchantmentSpecs.RAGE, CosmicEnchantmentSpecs.DOUBLESTRIKE,
                    CosmicEnchantmentSpecs.EXECUTE, CosmicEnchantmentSpecs.GREATSWORD, CosmicEnchantmentSpecs.SILENCE),
                    CosmicEnchantmentSpecs.DIVINE_IMMOLATION));

    public static final VKitDefinition OGRE = new VKitDefinition(
            CosmicPVE.id("ogre"), "Ogre", 0x18660A, "tooltip.cosmicpve.vkit.ogre",
            reward(Items.IRON_CHESTPLATE, "Fat Tummy", List.of(
                    CosmicEnchantmentSpecs.AEGIS, CosmicEnchantmentSpecs.ARMORED,
                    CosmicEnchantmentSpecs.ANGELIC, CosmicEnchantmentSpecs.LEADERSHIP, CosmicEnchantmentSpecs.OVERLOAD)),
            reward(Items.CROSSBOW, "The Gutbuster", List.of(
                    CosmicEnchantmentSpecs.VIRUS, CosmicEnchantmentSpecs.EAGLE_EYE,
                    CosmicEnchantmentSpecs.LIGHTNING, CosmicEnchantmentSpecs.VENOM,
                    CosmicEnchantmentSpecs.SNARE, CosmicEnchantmentSpecs.SNIPER)));

    public static final VKitDefinition JUDGEMENT = new VKitDefinition(
            CosmicPVE.id("judgement"), "Judgement", 0x663434, "tooltip.cosmicpve.vkit.judgement",
            reward(Items.IRON_LEGGINGS, "Trousers of Retribution", List.of(
                    CosmicEnchantmentSpecs.PLAGUE_CARRIER, CosmicEnchantmentSpecs.ARMORED,
                    CosmicEnchantmentSpecs.CACTUS, CosmicEnchantmentSpecs.SELF_DESTRUCT,
                    CosmicEnchantmentSpecs.MOLTEN, CosmicEnchantmentSpecs.LUCK)),
            reward(Items.DIAMOND_AXE, "The Banhammer", List.of(
                    CosmicEnchantmentSpecs.RAGE, CosmicEnchantmentSpecs.DEVOUR,
                    CosmicEnchantmentSpecs.PUMMEL, CosmicEnchantmentSpecs.INSANITY, CosmicEnchantmentSpecs.HEX),
                    CosmicEnchantmentSpecs.SOUL_TETHER));

    public static final VKitDefinition SLAYER = new VKitDefinition(
            CosmicPVE.id("slayer"), "Slayer", 0x8C0B0B, "tooltip.cosmicpve.vkit.slayer",
            reward(Items.IRON_HELMET, "Shroud of War", List.of(
                    CosmicEnchantmentSpecs.MOLTEN, CosmicEnchantmentSpecs.ARMORED,
                    CosmicEnchantmentSpecs.VOODOO, CosmicEnchantmentSpecs.ANGELIC, CosmicEnchantmentSpecs.DEATHBRINGER),
                    CosmicEnchantmentSpecs.MORTAL_COIL),
            reward(Items.BOW, "Glitched Bow", List.of(
                    CosmicEnchantmentSpecs.VIRUS, CosmicEnchantmentSpecs.SNIPER,
                    CosmicEnchantmentSpecs.LIGHTNING, CosmicEnchantmentSpecs.EAGLE_EYE, CosmicEnchantmentSpecs.SILENCE),
                    CosmicEnchantmentSpecs.SOUL_SIPHON));

    public static final List<VKitDefinition> ALL = List.of(PHOENIX, OGRE, JUDGEMENT, SLAYER);

    public static Optional<VKitDefinition> find(Identifier id) {
        return ALL.stream().filter(definition -> definition.id().equals(id)).findFirst();
    }

    private static EquipmentReward reward(Item item, String name, List<CosmicEnchantmentSpec> pool,
            CosmicEnchantmentSpec... levelEightAdditions) {
        return new EquipmentReward(item, name, List.copyOf(pool), List.of(levelEightAdditions));
    }

    public record EquipmentReward(
            Item item,
            String displayName,
            List<CosmicEnchantmentSpec> basePool,
            List<CosmicEnchantmentSpec> levelEightAdditions) {
        public EquipmentReward {
            basePool = unique(basePool);
            var canonicalBase = basePool;
            levelEightAdditions = unique(levelEightAdditions).stream()
                    .filter(spec -> canonicalBase.stream().noneMatch(base -> base.id().equals(spec.id()))).toList();
        }

        public List<CosmicEnchantmentSpec> pool(int kitLevel) {
            if (kitLevel < 8) return basePool;
            var result = new java.util.ArrayList<>(basePool);
            result.addAll(levelEightAdditions);
            return List.copyOf(result);
        }

        private static List<CosmicEnchantmentSpec> unique(List<CosmicEnchantmentSpec> pool) {
            var byId = new java.util.LinkedHashMap<Identifier, CosmicEnchantmentSpec>();
            pool.forEach(spec -> byId.putIfAbsent(spec.id(), spec));
            return List.copyOf(byId.values());
        }
    }
}
