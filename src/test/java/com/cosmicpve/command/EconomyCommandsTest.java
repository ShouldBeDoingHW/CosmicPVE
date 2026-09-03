package com.cosmicpve.command;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class EconomyCommandsTest {
    @Test void balancePresentationIsEntirelyBoldGreenWithoutChangingValue() {
        var component = EconomyCommands.balanceMessage(123_450);
        var contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        assertEquals("command.cosmicpve.balance", contents.getKey());
        assertArrayEquals(new Object[]{"$1,234.50"}, contents.getArgs());
        assertEquals(0x55FF55, component.getStyle().getColor().getValue());
        assertTrue(component.getStyle().isBold());
    }
}
