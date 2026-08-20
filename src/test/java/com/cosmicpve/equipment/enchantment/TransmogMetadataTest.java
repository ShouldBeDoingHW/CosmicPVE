package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

class TransmogMetadataTest {
    @Test void transmogFlagIsPersistentAndOldMetadataDefaultsToUnsorted() {
        var sorted = CustomEnchantMetadata.DEFAULT.withTransmogSorted(true);
        var encoded = CustomEnchantMetadata.CODEC.encodeStart(JsonOps.INSTANCE, sorted).getOrThrow();
        assertTrue(CustomEnchantMetadata.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().transmogSorted());
        var legacy = JsonParser.parseString("{\"data_version\":1,\"slot_limit\":5,\"orb_upgrades\":0}");
        assertFalse(CustomEnchantMetadata.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow().transmogSorted());
    }

    @Test void transmogMutationPreservesCapacityAndProtection() {
        var before = new CustomEnchantMetadata(2, 5, 3, true, false);
        var after = before.withTransmogSorted(true);
        assertEquals(before.slotLimit(), after.slotLimit());
        assertEquals(before.orbUpgrades(), after.orbUpgrades());
        assertEquals(before.whiteScrollProtected(), after.whiteScrollProtected());
        assertTrue(after.transmogSorted());
    }

    @Test void transmogUsesPlainPaperPresentation() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/transmog_scroll.json")))) {
            assertEquals("minecraft:item/paper", JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("textures").get("layer0").getAsString());
        }
    }
}
