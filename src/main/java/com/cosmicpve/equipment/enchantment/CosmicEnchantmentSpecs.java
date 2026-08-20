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
            ModEnchantments.POISON.identifier(), 3, CosmicEnchantmentTier.ELITE, "sword");
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

    public static final List<CosmicEnchantmentSpec> ALL =
            List.of(EXECUTE, ANGELIC, LIGHTNING, ENDER_SHIFT, DOUBLESTRIKE, BLEED, LUCK, POISON, PUMMEL,
                    GREATSWORD, INSANITY, VENOM, AEGIS, EAGLE_EYE, RAGE);

    private CosmicEnchantmentSpecs() {}

    public static Optional<CosmicEnchantmentSpec> find(Identifier id) {
        return ALL.stream().filter(spec -> spec.id().equals(id)).findFirst();
    }
}
