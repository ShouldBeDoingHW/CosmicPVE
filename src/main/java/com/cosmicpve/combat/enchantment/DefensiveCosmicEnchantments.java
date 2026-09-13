package com.cosmicpve.combat.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Explicit semantic classification shared by Neutralize and Nimble. */
public final class DefensiveCosmicEnchantments {
    public static final Identifier PROC_CLASSIFICATION =
            Identifier.fromNamespaceAndPath("cosmicpve", "defensive_cosmic_enchantment");
    public static final Set<Identifier> IDS = Set.of(
            ModEnchantments.AEGIS.identifier(), ModEnchantments.ANGELIC.identifier(),
            ModEnchantments.ARMORED.identifier(), ModEnchantments.CACTUS.identifier(),
            ModEnchantments.DEATH_PACT.identifier(), ModEnchantments.DODGE.identifier(),
            ModEnchantments.ENDER_SHIFT.identifier(), ModEnchantments.ENDER_WALKER.identifier(),
            ModEnchantments.INVERSION.identifier(), ModEnchantments.MOLTEN.identifier(),
            ModEnchantments.MORTAL_COIL.identifier(), ModEnchantments.OBSIDIANSHIELD.identifier(),
            ModEnchantments.PERMAFROST.identifier(), ModEnchantments.PHOENIX.identifier(),
            ModEnchantments.PLAGUE_CARRIER.identifier(), ModEnchantments.SELF_DESTRUCT.identifier(),
            ModEnchantments.SPIRIT_LINK.identifier(), ModEnchantments.STORMCALLER.identifier(),
            ModEnchantments.UNDEAD_RUSE.identifier(), ModEnchantments.VOODOO.identifier(),
            ModEnchantments.MIGHTY_CACTUS.identifier(), ModEnchantments.PALADIN_ARMORED.identifier());

    private DefensiveCosmicEnchantments() {}
    public static boolean contains(Identifier id) { return IDS.contains(id); }
    public static boolean defensive(com.cosmicpve.combat.proc.ProcCandidate candidate) {
        return candidate.classifications().contains(PROC_CLASSIFICATION)
                || contains(candidate.effectId()) || contains(candidate.provenance().sourceId());
    }
}
