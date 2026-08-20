package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.ArmorSetIdentity;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class ArmorSetColorResolverTest {
    @Test void colorsComeFromIdentityAndNoSetHasNoTint() {
        assertEquals(0xFF6969, ArmorSetColorResolver.color(identity(ArmorSetIds.PHANTOM, 0xFF6969)).orElseThrow());
        assertEquals(0xA3FFF5, ArmorSetColorResolver.color(identity(ArmorSetIds.YETI, 0xA3FFF5)).orElseThrow());
        assertTrue(ArmorSetColorResolver.color(null).isEmpty());
        assertEquals(0xFFFFFFFF, ArmorSetColorResolver.argbOrWhite(null));
    }

    private static ArmorSetIdentity identity(net.minecraft.resources.Identifier id, int color) {
        return new ArmorSetIdentity(1, id, Component.literal(id.toString()), color, List.of(Component.literal("bonus")));
    }
}
