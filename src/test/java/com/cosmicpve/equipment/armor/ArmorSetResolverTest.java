package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArmorSetResolverTest {
    @Test void fourMatchingPhantomActivates() { assertEquals(Optional.of(ArmorSetIds.PHANTOM), four(ArmorSetIds.PHANTOM)); }
    @Test void fourMatchingYetiActivates() { assertEquals(Optional.of(ArmorSetIds.YETI), four(ArmorSetIds.YETI)); }
    @Test void fourMatchingAncientActivates() { assertEquals(Optional.of(ArmorSetIds.ANCIENT), four(ArmorSetIds.ANCIENT)); }
    @Test void everyCanonicalNormalIdentityCanResolve() {
        for (var id : List.of(ArmorSetIds.PHANTOM, ArmorSetIds.YJIKI, ArmorSetIds.DIMENSIONAL_TRAVELER,
                ArmorSetIds.ENGINEER, ArmorSetIds.YETI, ArmorSetIds.ANCIENT, ArmorSetIds.RANGER,
                ArmorSetIds.DRAGONSLAYER)) assertEquals(Optional.of(id), four(id));
    }

    @Test void omniRequiresTwoMatchingOrdinaryAnchorsAndRejectsMixedAnchors() {
        assertEquals(Optional.of(ArmorSetIds.PHANTOM), ArmorSetResolver.resolvePieceIdentities(List.of(
                piece(ArmorSetIds.PHANTOM, false), piece(ArmorSetIds.PHANTOM, false),
                piece(null, true), piece(null, true))));
        assertEquals(Optional.of(ArmorSetIds.YETI), ArmorSetResolver.resolvePieceIdentities(List.of(
                piece(ArmorSetIds.YETI, false), piece(ArmorSetIds.YETI, false),
                piece(ArmorSetIds.YETI, false), piece(null, true))));
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(piece(ArmorSetIds.PHANTOM, false),
                piece(ArmorSetIds.PHANTOM, false), piece(null, true), piece(ArmorSetIds.DRAGONSLAYER, false))).isEmpty());
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(piece(ArmorSetIds.PHANTOM, false),
                piece(ArmorSetIds.YETI, false), piece(null, true), piece(null, true))).isEmpty());
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(piece(ArmorSetIds.PHANTOM, false),
                piece(null, true), piece(null, true), piece(null, true))).isEmpty());
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(piece(null, true), piece(null, true),
                piece(null, true), piece(null, true))).isEmpty());
    }
    @Test void mixedArmorMaterialsWithAncientIdentityActivate() {
        var identity = new com.cosmicpve.data.component.ArmorSetIdentity(1, ArmorSetIds.ANCIENT,
                net.minecraft.network.chat.Component.literal("Ancient"), 0x0A4A3D,
                List.of(net.minecraft.network.chat.Component.literal("bonus")));
        var pieces = List.of(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_LEGGINGS),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_BOOTS));
        pieces.forEach(piece -> piece.set(com.cosmicpve.registry.ModDataComponents.ARMOR_SET_ID.get(), identity));
        assertEquals(Optional.of(ArmorSetIds.ANCIENT), ArmorSetResolver.resolveIdentity(pieces));
    }
    @Test void threeAndOneActivatesNothing() { assertTrue(ArmorSetResolver.resolveIdentityIds(List.of(
            Optional.of(ArmorSetIds.PHANTOM), Optional.of(ArmorSetIds.PHANTOM),
            Optional.of(ArmorSetIds.PHANTOM), Optional.of(ArmorSetIds.YETI))).isEmpty()); }
    @Test void twoAndTwoActivatesNothing() { assertTrue(ArmorSetResolver.resolveIdentityIds(List.of(
            Optional.of(ArmorSetIds.PHANTOM), Optional.of(ArmorSetIds.PHANTOM),
            Optional.of(ArmorSetIds.YETI), Optional.of(ArmorSetIds.YETI))).isEmpty()); }
    @Test void missingPieceActivatesNothing() { assertTrue(ArmorSetResolver.resolveIdentityIds(List.of(
            Optional.of(ArmorSetIds.YETI), Optional.of(ArmorSetIds.YETI),
            Optional.of(ArmorSetIds.YETI), Optional.empty())).isEmpty()); }

    private static Optional<net.minecraft.resources.Identifier> four(net.minecraft.resources.Identifier id) {
        return ArmorSetResolver.resolveIdentityIds(List.of(Optional.of(id), Optional.of(id), Optional.of(id), Optional.of(id)));
    }
    private static ArmorSetResolver.PieceIdentity piece(net.minecraft.resources.Identifier id, boolean omni) {
        return new ArmorSetResolver.PieceIdentity(Optional.ofNullable(id), omni);
    }
}
