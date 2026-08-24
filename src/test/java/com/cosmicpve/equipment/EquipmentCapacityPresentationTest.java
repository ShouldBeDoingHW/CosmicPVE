package com.cosmicpve.equipment;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EquipmentCapacityPresentationTest {
    @Test void baseAndOrbCapacityLinesUseSettledText() {
        assertEquals("5 Enchantment Slots",EquipmentTooltipService.capacityLine(5).getString());
        assertEquals("6 Enchantment Slots (Orb [+1])",EquipmentTooltipService.capacityLine(6).getString());
        assertEquals("8 Enchantment Slots (Orb [+3])",EquipmentTooltipService.capacityLine(8).getString());
        assertEquals("10 Enchantment Slots (Orb [+5])",EquipmentTooltipService.capacityLine(10).getString());
        assertFalse(EquipmentTooltipService.capacityLine(5).getString().contains("Orb"));
        assertFalse(EquipmentTooltipService.capacityLine(10).getString().contains("Cosmic Enchants"));
        assertEquals(0x55FF55,EquipmentTooltipService.capacityLine(5).getStyle().getColor().getValue());
    }
}
