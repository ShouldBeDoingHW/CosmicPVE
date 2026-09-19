package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Canonical one-to-one ordinary/Heroic replacement relationships. */
public final class HeroicEnchantments {
    public record Pair(Identifier ordinary, Identifier heroic) {}
    public static final List<Pair> PAIRS = List.of(
            pair(ModEnchantments.BLEED, ModEnchantments.DEEP_BLEED),
            pair(ModEnchantments.CACTUS, ModEnchantments.MIGHTY_CACTUS),
            pair(ModEnchantments.ARMORED, ModEnchantments.PALADIN_ARMORED),
            pair(ModEnchantments.VIRUS, ModEnchantments.BLIGHTED_VIRUS),
            pair(ModEnchantments.IMPLANTS, ModEnchantments.ALIEN_IMPLANTS),
            pair(ModEnchantments.SNIPER, ModEnchantments.LETHAL_SNIPER),
            pair(ModEnchantments.SNARE, ModEnchantments.ETERNAL_SNARE),
            pair(ModEnchantments.EXECUTE, ModEnchantments.PERMANENT_EXECUTE),
            pair(ModEnchantments.CLEAVE, ModEnchantments.MIGHTY_CLEAVE),
            pair(ModEnchantments.CURSE, ModEnchantments.FORBIDDEN_CURSE),
            pair(ModEnchantments.OVERLOAD, ModEnchantments.GODLY_OVERLOAD),
            pair(ModEnchantments.DEATHBRINGER, ModEnchantments.PLANETARY_DEATHBRINGER));
    private static final Map<Identifier, Identifier> ORDINARY_TO_HEROIC = PAIRS.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(Pair::ordinary, Pair::heroic));
    private static final Map<Identifier, Identifier> HEROIC_TO_ORDINARY = PAIRS.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(Pair::heroic, Pair::ordinary));

    private HeroicEnchantments() {}
    private static Pair pair(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> ordinary,
            net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> heroic) {
        return new Pair(ordinary.identifier(), heroic.identifier());
    }
    public static Optional<Identifier> heroicFor(Identifier ordinary) { return Optional.ofNullable(ORDINARY_TO_HEROIC.get(ordinary)); }
    public static Optional<Identifier> ordinaryFor(Identifier heroic) { return Optional.ofNullable(HEROIC_TO_ORDINARY.get(heroic)); }
    public static boolean isHeroic(Identifier id) { return HEROIC_TO_ORDINARY.containsKey(id); }
}
