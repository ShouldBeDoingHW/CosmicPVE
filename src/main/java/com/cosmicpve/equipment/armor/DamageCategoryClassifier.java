package com.cosmicpve.equipment.armor;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Typed classification for the narrow Cosmic categorical-reduction rules. */
public final class DamageCategoryClassifier {
    public enum Category { FIRE, LAVA, POISON, WITHER, OTHER }

    private DamageCategoryClassifier() {}

    public static Category classify(DamageSource source) {
        if (source.is(DamageTypes.LAVA)) return Category.LAVA;
        if (source.is(NeoForgeMod.POISON_DAMAGE)) return Category.POISON;
        if (source.is(DamageTypes.WITHER)) return Category.WITHER;
        if (source.is(DamageTypeTags.IS_FIRE)) return Category.FIRE;
        return Category.OTHER;
    }
}
