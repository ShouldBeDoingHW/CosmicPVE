package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class WhiteScrollMetadataTest {
    @Test void protectedStateSurvivesCodecRoundTrip() {
        var protectedData = new CustomEnchantMetadata(2, 5, 0, true, false);
        var encoded = CustomEnchantMetadata.CODEC.encodeStart(JsonOps.INSTANCE, protectedData).getOrThrow();
        var decoded = CustomEnchantMetadata.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(decoded.whiteScrollProtected());
        assertEquals(5, decoded.slotLimit());
    }

    @Test void whiteScrollNameIsBold() {
        var name = new ItemStack(ModItems.WHITE_SCROLL.get()).getHoverName();
        assertTrue(name.getStyle().isBold());
        assertEquals(0xFFFFFF, name.getStyle().getColor().getValue());
        assertEquals("A thin veil between fortune and ruin.", WhiteScrollItem.lore().getFirst().getString());
        assertTrue(WhiteScrollItem.lore().getFirst().getStyle().isItalic());
        assertEquals("ONE-TIME PROTECTION", WhiteScrollItem.lore().get(2).getString());
        assertTrue(WhiteScrollItem.lore().get(2).getStyle().isBold());
    }
}
