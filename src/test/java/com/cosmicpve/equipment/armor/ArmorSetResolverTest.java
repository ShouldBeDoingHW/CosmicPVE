package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArmorSetResolverTest {
    @Test void fourMatchingPhantomActivates() { assertEquals(Optional.of(ArmorSetIds.PHANTOM), four(ArmorSetIds.PHANTOM)); }
    @Test void fourMatchingYetiActivates() { assertEquals(Optional.of(ArmorSetIds.YETI), four(ArmorSetIds.YETI)); }
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
