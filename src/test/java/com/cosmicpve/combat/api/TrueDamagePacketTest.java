package com.cosmicpve.combat.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import org.junit.jupiter.api.Test;

class TrueDamagePacketTest {
    @Test
    void standardTrueDamageConsumesAbsorptionByDefault() {
        var packet = TrueDamagePacket.standard(CosmicPVE.id("test/true"), 2.0);
        assertTrue(packet.bypassesArmor());
        assertTrue(packet.bypassesCustomReduction());
        assertFalse(packet.bypassesAbsorption());
    }
}
