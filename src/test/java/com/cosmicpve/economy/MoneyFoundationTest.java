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

    @Test void shorthandAndConventionalCommaGroupingRemainExact() {
        assertEquals(MoneyAmount.parseCents("100000"), MoneyAmount.parseCents("100,000"));
        assertEquals(MoneyAmount.parseCents("1000000"), MoneyAmount.parseCents("1,000,000"));
        assertEquals(MoneyAmount.parseCents("1234567.89"), MoneyAmount.parseCents("1,234,567.89"));
        assertEquals(123_456L, MoneyAmount.parseCents("1234.56"));
        assertEquals(10_000_000L, MoneyAmount.parseCents("100k"));
        assertEquals(150_000L, MoneyAmount.parseCents("1.5K"));
        assertEquals(1_225_000L, MoneyAmount.parseCents("12.25k"));
        assertEquals(100_000_000L, MoneyAmount.parseCents("1m"));
        assertEquals(122_000_000L, MoneyAmount.parseCents("1.22M"));
        assertEquals(250_000_000L, MoneyAmount.parseCents("2.5m"));
        assertEquals(50_000_000L, MoneyAmount.parseCents("0.5m"));
        assertEquals(123_456_789L, MoneyAmount.parseCents("1,234,567.89"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1,00"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1,,000"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1,23,456"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1b"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1mm"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("1..2m"));
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parseCents("0.000001k"));
    }

    @Test void withdrawalResolutionSupportsAllMaxAndRejectsWithoutMutationPlan() {
        long balance = 123_456_789L;
        assertEquals(new WithdrawalService.Resolution(WithdrawalService.Status.SUCCESS, balance),
                WithdrawalService.resolve("all", balance));
        assertEquals(new WithdrawalService.Resolution(WithdrawalService.Status.SUCCESS, balance),
                WithdrawalService.resolve("MAX", balance));
        assertEquals(WithdrawalService.Status.INSUFFICIENT_FUNDS,
                WithdrawalService.resolve("all", 0).status());
        assertEquals(new WithdrawalService.Resolution(WithdrawalService.Status.SUCCESS, 122_000_000L),
                WithdrawalService.resolve("1.22m", balance));
        assertEquals(WithdrawalService.Status.INSUFFICIENT_FUNDS,
                WithdrawalService.resolve("2m", balance).status());
        assertEquals(WithdrawalService.Status.INVALID, WithdrawalService.resolve("abc", balance).status());
        assertEquals(WithdrawalService.Status.INVALID, WithdrawalService.resolve("0", balance).status());
        assertEquals(WithdrawalService.Status.INVALID, WithdrawalService.resolve("-1", balance).status());
    }

    @Test void overflowAndMalformedBanknotesAreRejected() {
        assertTrue(MoneyService.canAdd(0, 1));
        assertFalse(MoneyService.canAdd(Long.MAX_VALUE, 1));
        assertThrows(IllegalArgumentException.class, () -> new BanknoteData(1, 0));
        var malformed = com.google.gson.JsonParser.parseString("{\"value_cents\":-1}");
        assertTrue(BanknoteData.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
    }
}
