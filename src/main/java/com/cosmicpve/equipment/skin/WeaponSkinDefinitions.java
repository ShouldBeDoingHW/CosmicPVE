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
    public static final Identifier GRIM_AXE = CosmicPVE.id("grim_axe");
    public static final Identifier WHISK_TAKER = CosmicPVE.id("whisk_taker");
    public static final Identifier THE_CARVER = CosmicPVE.id("the_carver");
    public static final Identifier SPINAL_TAP = CosmicPVE.id("spinal_tap");
    public static final Identifier FIREWORK_ROCKET = CosmicPVE.id("firework_rocket");
    public static final Identifier TRIDENT_OF_THE_DEEP = CosmicPVE.id("trident_of_the_deep");

    private static final Map<Identifier, WeaponSkinDefinition> DEFINITIONS = List.of(
            definition(BOOSTED_CHAINSAW, 0xCCA00A, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.boosted_chainsaw.effect")),
                    List.of(new VirtualEnchantmentGrant(
                            ModEnchantments.DOUBLESTRIKE.identifier(), 5, BOOSTED_CHAINSAW))),
            definition(MAUIS_HOOK, 0x404242, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.mauis_hook.effect.damage"),
                            Component.translatable("weapon_skin.cosmicpve.mauis_hook.effect.steal")), List.of()),
            definition(STORMBRINGER, 0x224B57, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.stormbringer.effect.lightning"),
                            Component.translatable("weapon_skin.cosmicpve.stormbringer.effect.defense")), List.of()),
            definition(SEASONS_BEATINGS, 0x1B943A, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.seasons_beatings.effect.defense"),
                            Component.translatable("weapon_skin.cosmicpve.seasons_beatings.effect.slowness")), List.of()),
            definition(GRIM_AXE, 0x4C09B8, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.grim_axe.effect")), List.of()),
            definition(WHISK_TAKER, 0xB08E00, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.whisk_taker.effect.frenzy"),
                            Component.translatable("weapon_skin.cosmicpve.whisk_taker.effect.bonus")), List.of()),
            definition(THE_CARVER, 0xDBD70B, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.the_carver.effect.devour"),
                            Component.translatable("weapon_skin.cosmicpve.the_carver.effect.damage")),
                    List.of(new VirtualEnchantmentGrant(ModEnchantments.DEVOUR.identifier(), 4, THE_CARVER))),
            definition(SPINAL_TAP, 0x00F02C, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.spinal_tap.effect")), List.of()),
            definition(TRIDENT_OF_THE_DEEP, 0x10B29C, WeaponSkinDefinition.WeaponKind.SWORD,
                    List.of(Component.translatable("weapon_skin.cosmicpve.trident_of_the_deep.effect")),
                    List.of(new VirtualEnchantmentGrant(
                            ModEnchantments.BLACKOUT.identifier(), 6, TRIDENT_OF_THE_DEEP))),
            definition(FIREWORK_ROCKET, 0xD43700, WeaponSkinDefinition.WeaponKind.AXE,
                    List.of(Component.translatable("weapon_skin.cosmicpve.firework_rocket.effect")),
                    List.of(new VirtualEnchantmentGrant(
                            ModEnchantments.TITAN_TRAP.identifier(), 3, FIREWORK_ROCKET)))
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
