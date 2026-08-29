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
        assertTrue(new ItemStack(ModItems.WHITE_SCROLL.get()).getHoverName().getStyle().isBold());
    }
}
