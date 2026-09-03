package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Cosmic metadata not represented by Minecraft's dynamic enchantment record. */
public final class CosmicEnchantmentSpecs {
    public static final CosmicEnchantmentSpec EXECUTE = new CosmicEnchantmentSpec(
            ModEnchantments.EXECUTE.identifier(), 5, CosmicEnchantmentTier.ELITE, "sword");
    public static final CosmicEnchantmentSpec ANGELIC = new CosmicEnchantmentSpec(
            ModEnchantments.ANGELIC.identifier(), 5, CosmicEnchantmentTier.ULTIMATE, "any_armor");
    public static final CosmicEnchantmentSpec LIGHTNING = new CosmicEnchantmentSpec(
            ModEnchantments.LIGHTNING.identifier(), 4, CosmicEnchantmentTier.SIMPLE, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec ENDER_SHIFT = new CosmicEnchantmentSpec(
            ModEnchantments.ENDER_SHIFT.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "helmet");
    public static final CosmicEnchantmentSpec DOUBLESTRIKE = new CosmicEnchantmentSpec(
            ModEnchantments.DOUBLESTRIKE.identifier(), 3, CosmicEnchantmentTier.LEGENDARY, "sword");
    public static final CosmicEnchantmentSpec BLEED = new CosmicEnchantmentSpec(
            ModEnchantments.BLEED.identifier(), 6, CosmicEnchantmentTier.ULTIMATE, "axe");
    public static final CosmicEnchantmentSpec LUCK = new CosmicEnchantmentSpec(
            ModEnchantments.LUCK.identifier(), 10, CosmicEnchantmentTier.ULTIMATE, "boots_or_leggings");
    public static final CosmicEnchantmentSpec POISON = new CosmicEnchantmentSpec(
            ModEnchantments.POISON.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "sword");
    public static final CosmicEnchantmentSpec PUMMEL = new CosmicEnchantmentSpec(
            ModEnchantments.PUMMEL.identifier(), 3, CosmicEnchantmentTier.ELITE, "axe");
    public static final CosmicEnchantmentSpec GREATSWORD = new CosmicEnchantmentSpec(
            ModEnchantments.GREATSWORD.identifier(), 4, CosmicEnchantmentTier.ELITE, "sword");
    public static final CosmicEnchantmentSpec INSANITY = new CosmicEnchantmentSpec(
            ModEnchantments.INSANITY.identifier(), 8, CosmicEnchantmentTier.LEGENDARY, "axe");
    public static final CosmicEnchantmentSpec VENOM = new CosmicEnchantmentSpec(
            ModEnchantments.VENOM.identifier(), 3, CosmicEnchantmentTier.ELITE, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec AEGIS = new CosmicEnchantmentSpec(
            ModEnchantments.AEGIS.identifier(), 6, CosmicEnchantmentTier.LEGENDARY, "chestplate");
    public static final CosmicEnchantmentSpec EAGLE_EYE = new CosmicEnchantmentSpec(
            ModEnchantments.EAGLE_EYE.identifier(), 6, CosmicEnchantmentTier.ULTIMATE, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec RAGE = new CosmicEnchantmentSpec(
            ModEnchantments.RAGE.identifier(), 6, CosmicEnchantmentTier.LEGENDARY, "sword_or_axe");
    public static final CosmicEnchantmentSpec MOLTEN = new CosmicEnchantmentSpec(
            ModEnchantments.MOLTEN.identifier(), 4, CosmicEnchantmentTier.UNIQUE, "any_armor");
    public static final CosmicEnchantmentSpec NUTRITION = new CosmicEnchantmentSpec(
            ModEnchantments.NUTRITION.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "leggings");
    public static final CosmicEnchantmentSpec GLOWING = new CosmicEnchantmentSpec(
            ModEnchantments.GLOWING.identifier(), 1, CosmicEnchantmentTier.SIMPLE, "helmet");
    public static final CosmicEnchantmentSpec OBSIDIANSHIELD = new CosmicEnchantmentSpec(
            ModEnchantments.OBSIDIANSHIELD.identifier(), 2, CosmicEnchantmentTier.ULTIMATE, "leggings");
    public static final CosmicEnchantmentSpec OXYGENATE = new CosmicEnchantmentSpec(
            ModEnchantments.OXYGENATE.identifier(), 2, CosmicEnchantmentTier.SIMPLE, "pickaxe");
    public static final CosmicEnchantmentSpec ARMORED = new CosmicEnchantmentSpec(
            ModEnchantments.ARMORED.identifier(), 4, CosmicEnchantmentTier.LEGENDARY, "any_armor");
    public static final CosmicEnchantmentSpec DEATH_PACT = new CosmicEnchantmentSpec(
            ModEnchantments.DEATH_PACT.identifier(), 5, CosmicEnchantmentTier.MASTERY, "chestplate");
    public static final CosmicEnchantmentSpec AUTO_SMELT = new CosmicEnchantmentSpec(
            ModEnchantments.AUTO_SMELT.identifier(), 1, CosmicEnchantmentTier.ULTIMATE, "pickaxe");
    public static final CosmicEnchantmentSpec EXPERIENCE = new CosmicEnchantmentSpec(
            ModEnchantments.EXPERIENCE.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "pickaxe");
    public static final CosmicEnchantmentSpec BLESSED = new CosmicEnchantmentSpec(
            ModEnchantments.BLESSED.identifier(), 4, CosmicEnchantmentTier.ULTIMATE, "axe");
    public static final CosmicEnchantmentSpec IMPLANTS = new CosmicEnchantmentSpec(
            ModEnchantments.IMPLANTS.identifier(), 3, CosmicEnchantmentTier.ULTIMATE, "helmet");
    public static final CosmicEnchantmentSpec TRAP = new CosmicEnchantmentSpec(
            ModEnchantments.TRAP.identifier(), 3, CosmicEnchantmentTier.ELITE, "sword");
    public static final CosmicEnchantmentSpec CACTUS = new CosmicEnchantmentSpec(
            ModEnchantments.CACTUS.identifier(), 2, CosmicEnchantmentTier.ELITE, "leggings");
    public static final CosmicEnchantmentSpec GEARS = new CosmicEnchantmentSpec(
            ModEnchantments.GEARS.identifier(), 3, CosmicEnchantmentTier.LEGENDARY, "boots");
    public static final CosmicEnchantmentSpec PERMAFROST = new CosmicEnchantmentSpec(
            ModEnchantments.PERMAFROST.identifier(), 6, CosmicEnchantmentTier.MASTERY, "chestplate");
    public static final CosmicEnchantmentSpec MORTAL_COIL = new CosmicEnchantmentSpec(
            ModEnchantments.MORTAL_COIL.identifier(), 2, CosmicEnchantmentTier.MASTERY, "helmet");
    public static final CosmicEnchantmentSpec SELF_DESTRUCT = new CosmicEnchantmentSpec(
            ModEnchantments.SELF_DESTRUCT.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "leggings");
    public static final CosmicEnchantmentSpec PHOENIX = new CosmicEnchantmentSpec(
            ModEnchantments.PHOENIX.identifier(), 3, CosmicEnchantmentTier.MASTERY, "boots");
    public static final CosmicEnchantmentSpec DIVINE_IMMOLATION = new CosmicEnchantmentSpec(
            ModEnchantments.DIVINE_IMMOLATION.identifier(), 4, CosmicEnchantmentTier.MASTERY, "sword");
    public static final CosmicEnchantmentSpec VIRUS = new CosmicEnchantmentSpec(
            ModEnchantments.VIRUS.identifier(), 3, CosmicEnchantmentTier.UNIQUE, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec DEVOUR = new CosmicEnchantmentSpec(
            ModEnchantments.DEVOUR.identifier(), 4, CosmicEnchantmentTier.LEGENDARY, "axe");
    public static final CosmicEnchantmentSpec UNDEAD_RUSE = new CosmicEnchantmentSpec(
            ModEnchantments.UNDEAD_RUSE.identifier(), 10, CosmicEnchantmentTier.ELITE, "any_armor");
    public static final CosmicEnchantmentSpec OBLITERATE = new CosmicEnchantmentSpec(
            ModEnchantments.OBLITERATE.identifier(), 3, CosmicEnchantmentTier.SIMPLE, "all_weapons");
    public static final CosmicEnchantmentSpec SOUL_TETHER = new CosmicEnchantmentSpec(
            ModEnchantments.SOUL_TETHER.identifier(), 3, CosmicEnchantmentTier.MASTERY, "axe");
    public static final CosmicEnchantmentSpec DODGE = new CosmicEnchantmentSpec(
            ModEnchantments.DODGE.identifier(), 5, CosmicEnchantmentTier.ULTIMATE, "boots");
    public static final CosmicEnchantmentSpec LEADERSHIP = new CosmicEnchantmentSpec(
            ModEnchantments.LEADERSHIP.identifier(), 10, CosmicEnchantmentTier.LEGENDARY, "chestplate_or_leggings");
    public static final CosmicEnchantmentSpec HERO_KILLER = new CosmicEnchantmentSpec(
            ModEnchantments.HERO_KILLER.identifier(), 3, CosmicEnchantmentTier.MASTERY, "axe");
    public static final CosmicEnchantmentSpec SOUL_SIPHON = new CosmicEnchantmentSpec(
            ModEnchantments.SOUL_SIPHON.identifier(), 4, CosmicEnchantmentTier.MASTERY, "all_weapons");
    public static final CosmicEnchantmentSpec BLACKOUT = new CosmicEnchantmentSpec(
            ModEnchantments.BLACKOUT.identifier(), 4, CosmicEnchantmentTier.MASTERY, "sword");
    public static final CosmicEnchantmentSpec ENDER_WALKER = new CosmicEnchantmentSpec(
            ModEnchantments.ENDER_WALKER.identifier(), 5, CosmicEnchantmentTier.ULTIMATE, "boots");
    public static final CosmicEnchantmentSpec VOODOO = new CosmicEnchantmentSpec(
            ModEnchantments.VOODOO.identifier(), 6, CosmicEnchantmentTier.ELITE, "helmet");
    public static final CosmicEnchantmentSpec SNIPER = new CosmicEnchantmentSpec(
            ModEnchantments.SNIPER.identifier(), 5, CosmicEnchantmentTier.LEGENDARY, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec SNARE = new CosmicEnchantmentSpec(
            ModEnchantments.SNARE.identifier(), 4, CosmicEnchantmentTier.ELITE, "crossbow");
    public static final CosmicEnchantmentSpec PLAGUE_CARRIER = new CosmicEnchantmentSpec(
            ModEnchantments.PLAGUE_CARRIER.identifier(), 7, CosmicEnchantmentTier.UNIQUE, "leggings");
    public static final CosmicEnchantmentSpec OBSIDIAN_DESTROYER = new CosmicEnchantmentSpec(
            ModEnchantments.OBSIDIAN_DESTROYER.identifier(), 4, CosmicEnchantmentTier.UNIQUE, "pickaxe");
    public static final CosmicEnchantmentSpec DOMINATE = new CosmicEnchantmentSpec(
            ModEnchantments.DOMINATE.identifier(), 4, CosmicEnchantmentTier.ULTIMATE, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec HEX = new CosmicEnchantmentSpec(
            ModEnchantments.HEX.identifier(), 5, CosmicEnchantmentTier.LEGENDARY, "axe");
    public static final CosmicEnchantmentSpec STORMCALLER = new CosmicEnchantmentSpec(
            ModEnchantments.STORMCALLER.identifier(), 5, CosmicEnchantmentTier.ELITE, "boots_or_chestplate");
    public static final CosmicEnchantmentSpec INVERSION = new CosmicEnchantmentSpec(
            ModEnchantments.INVERSION.identifier(), 4, CosmicEnchantmentTier.ULTIMATE, "sword");
    public static final CosmicEnchantmentSpec SPIRIT_LINK = new CosmicEnchantmentSpec(
            ModEnchantments.SPIRIT_LINK.identifier(), 7, CosmicEnchantmentTier.ELITE, "helmet_or_chestplate");
    public static final CosmicEnchantmentSpec DEEP_BLEED = new CosmicEnchantmentSpec(
            ModEnchantments.DEEP_BLEED.identifier(), 6, CosmicEnchantmentTier.HEROIC, "axe");
    public static final CosmicEnchantmentSpec MIGHTY_CACTUS = new CosmicEnchantmentSpec(
            ModEnchantments.MIGHTY_CACTUS.identifier(), 2, CosmicEnchantmentTier.HEROIC, "leggings");
    public static final CosmicEnchantmentSpec PALADIN_ARMORED = new CosmicEnchantmentSpec(
            ModEnchantments.PALADIN_ARMORED.identifier(), 4, CosmicEnchantmentTier.HEROIC, "any_armor");
    public static final CosmicEnchantmentSpec BLIGHTED_VIRUS = new CosmicEnchantmentSpec(
            ModEnchantments.BLIGHTED_VIRUS.identifier(), 3, CosmicEnchantmentTier.HEROIC, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec ALIEN_IMPLANTS = new CosmicEnchantmentSpec(
            ModEnchantments.ALIEN_IMPLANTS.identifier(), 3, CosmicEnchantmentTier.HEROIC, "helmet");
    public static final CosmicEnchantmentSpec LETHAL_SNIPER = new CosmicEnchantmentSpec(
            ModEnchantments.LETHAL_SNIPER.identifier(), 5, CosmicEnchantmentTier.HEROIC, "bow_or_crossbow");
    public static final CosmicEnchantmentSpec ETERNAL_SNARE = new CosmicEnchantmentSpec(
            ModEnchantments.ETERNAL_SNARE.identifier(), 4, CosmicEnchantmentTier.HEROIC, "crossbow");
    public static final CosmicEnchantmentSpec PERMANENT_EXECUTE = new CosmicEnchantmentSpec(
            ModEnchantments.PERMANENT_EXECUTE.identifier(), 5, CosmicEnchantmentTier.HEROIC, "sword");

    public static final List<CosmicEnchantmentSpec> ALL =
            List.of(EXECUTE, ANGELIC, LIGHTNING, ENDER_SHIFT, DOUBLESTRIKE, BLEED, LUCK, POISON, PUMMEL,
                    GREATSWORD, INSANITY, VENOM, AEGIS, EAGLE_EYE, RAGE, MOLTEN, NUTRITION, GLOWING,
                    OBSIDIANSHIELD, OXYGENATE, ARMORED, DEATH_PACT, AUTO_SMELT, EXPERIENCE, BLESSED, IMPLANTS, TRAP, CACTUS,
                    GEARS, PERMAFROST, MORTAL_COIL, SELF_DESTRUCT, PHOENIX, DIVINE_IMMOLATION, VIRUS, DEVOUR,
                    UNDEAD_RUSE, OBLITERATE, SOUL_TETHER, DODGE, LEADERSHIP,
                    HERO_KILLER, SOUL_SIPHON, BLACKOUT, ENDER_WALKER, VOODOO,
                    SNIPER, SNARE, PLAGUE_CARRIER, OBSIDIAN_DESTROYER, DOMINATE, HEX, STORMCALLER, INVERSION, SPIRIT_LINK,
                    DEEP_BLEED, MIGHTY_CACTUS, PALADIN_ARMORED, BLIGHTED_VIRUS, ALIEN_IMPLANTS,
                    LETHAL_SNIPER, ETERNAL_SNARE, PERMANENT_EXECUTE);

    private CosmicEnchantmentSpecs() {}

    public static Optional<CosmicEnchantmentSpec> find(Identifier id) {
        return ALL.stream().filter(spec -> spec.id().equals(id)).findFirst();
    }
}
