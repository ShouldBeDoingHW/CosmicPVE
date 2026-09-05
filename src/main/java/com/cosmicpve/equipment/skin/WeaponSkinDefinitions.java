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
    public static final Identifier SEASONS_BEATINGS = CosmicPVE.id("seasons_beatings");

    private static final Map<Identifier, WeaponSkinDefinition> DEFINITIONS = List.of(
            definition(BOOSTED_CHAINSAW, 0xCCA00A, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.boosted_chainsaw.effect")),
                    List.of(new VirtualEnchantmentGrant(
                            ModEnchantments.DOUBLESTRIKE.identifier(), 3, BOOSTED_CHAINSAW))),
            definition(MAUIS_HOOK, 0x404242, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.mauis_hook.effect.damage"),
                            Component.translatable("weapon_skin.cosmicpve.mauis_hook.effect.steal")), List.of()),
            definition(STORMBRINGER, 0x224B57, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.stormbringer.effect.lightning"),
                            Component.translatable("weapon_skin.cosmicpve.stormbringer.effect.defense")), List.of()),
            definition(SEASONS_BEATINGS, 0x1B943A, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.seasons_beatings.effect.defense"),
                            Component.translatable("weapon_skin.cosmicpve.seasons_beatings.effect.slowness")), List.of())
    ).stream().collect(Collectors.toUnmodifiableMap(WeaponSkinDefinition::id, Function.identity()));

    private WeaponSkinDefinitions() {}

    public static Optional<WeaponSkinDefinition> find(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static List<Identifier> ids() {
        return DEFINITIONS.keySet().stream().sorted(Comparator.comparing(Identifier::toString)).toList();
    }

    private static WeaponSkinDefinition definition(
            Identifier id, int nameColor, WeaponSkinDefinition.WeaponKind kind,
            List<Component> effectDescription, List<VirtualEnchantmentGrant> grants) {
        return new WeaponSkinDefinition(id,
                Component.translatable("weapon_skin." + id.getNamespace() + "." + id.getPath()),
                nameColor, effectDescription, kind, id, grants);
    }
}
