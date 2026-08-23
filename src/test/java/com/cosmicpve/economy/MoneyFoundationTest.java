package com.cosmicpve.economy;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.attachment.PlayerProfileData;
import com.cosmicpve.data.component.BanknoteData;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class MoneyFoundationTest {
    @Test void defaultAndCodecPersistExactNonNegativeCents() {
        assertEquals(0, PlayerProfileData.createDefault().moneyCents());
        var data = new PlayerProfileData(1, 123456L);
        var encoded = PlayerProfileData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, PlayerProfileData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertThrows(IllegalArgumentException.class, () -> new PlayerProfileData(1, -1));
    }

    @Test void parsingAndFormattingAreExactAndNeverFloatingPoint() {
        assertEquals(125, MoneyAmount.parseCents("$1.25"));
        assertEquals(5_000_000, MoneyAmount.parseCents("50,000"));
        assertEquals("$0", MoneyAmount.format(0));
        assertEquals("$1,234.50", MoneyAmount.format(123450));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1.001"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("-1"));
    }

    @Test void overflowAndMalformedBanknotesAreRejected() {
        assertTrue(MoneyService.canAdd(0, 1));
        assertFalse(MoneyService.canAdd(Long.MAX_VALUE, 1));
        assertThrows(IllegalArgumentException.class, () -> new BanknoteData(1, 0));
        var malformed = com.google.gson.JsonParser.parseString("{\"value_cents\":-1}");
        assertTrue(BanknoteData.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
    }
}
