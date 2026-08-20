package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArmorSetResolverTest {
    @Test void fourMatchingPhantomActivates() { assertEquals(Optional.of(ArmorSetIds.PHANTOM), four(ArmorSetIds.PHANTOM)); }
    @Test void fourMatchingYetiActivates() { assertEquals(Optional.of(ArmorSetIds.YETI), four(ArmorSetIds.YETI)); }
    @Test void fourMatchingAncientActivates() { assertEquals(Optional.of(ArmorSetIds.ANCIENT), four(ArmorSetIds.ANCIENT)); }
    @Test void mixedArmorMaterialsWithAncientIdentityActivate() {
        var identity = new com.cosmicpve.data.component.ArmorSetIdentity(1, ArmorSetIds.ANCIENT,
                net.minecraft.network.chat.Component.literal("Ancient"), 0x050C59,
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
}
