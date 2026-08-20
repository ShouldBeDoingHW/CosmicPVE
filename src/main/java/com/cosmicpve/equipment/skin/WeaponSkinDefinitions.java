package com.cosmicpve.equipment.skin;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import com.cosmicpve.registry.ModEnchantments;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Explicit catalog of stable skin identities; bespoke behavior remains in small Java modules. */
public final class WeaponSkinDefinitions {
    public static final Identifier BOOSTED_CHAINSAW = CosmicPVE.id("boosted_chainsaw");
    public static final Identifier MAUIS_HOOK = CosmicPVE.id("mauis_hook");
    public static final Identifier STORMBRINGER = CosmicPVE.id("stormbringer");

    private static final Map<Identifier, WeaponSkinDefinition> DEFINITIONS = List.of(
            definition(BOOSTED_CHAINSAW, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(new VirtualEnchantmentGrant(
                            ModEnchantments.DOUBLESTRIKE.identifier(), 3, BOOSTED_CHAINSAW))),
            definition(MAUIS_HOOK, WeaponSkinDefinition.WeaponKind.SWORD, List.of()),
            definition(STORMBRINGER, WeaponSkinDefinition.WeaponKind.AXE, List.of())
    ).stream().collect(Collectors.toUnmodifiableMap(WeaponSkinDefinition::id, Function.identity()));

    private WeaponSkinDefinitions() {}

    public static Optional<WeaponSkinDefinition> find(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static List<Identifier> ids() {
        return DEFINITIONS.keySet().stream().sorted(Comparator.comparing(Identifier::toString)).toList();
    }

    private static WeaponSkinDefinition definition(
            Identifier id, WeaponSkinDefinition.WeaponKind kind, List<VirtualEnchantmentGrant> grants) {
        return new WeaponSkinDefinition(id,
                Component.translatable("weapon_skin." + id.getNamespace() + "." + id.getPath()),
                kind, id, grants);
    }
}
