package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Standard generated-weapon enchant package, with Mastery-only Mending where legal. */
public enum VanillaWeaponEnchantProfile {
    NONE, STANDARD, STANDARD_MASTERY;

    public static final Codec<VanillaWeaponEnchantProfile> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown vanilla weapon enchant profile: " + value);
        }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
