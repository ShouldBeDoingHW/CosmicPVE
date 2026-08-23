package com.cosmicpve.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyAmount {
    private MoneyAmount() {}

    public static long parseCents(String input) {
        String normalized = input.trim().replace("$", "").replace(",", "");
        try {
            BigDecimal dollars = new BigDecimal(normalized).setScale(2, RoundingMode.UNNECESSARY);
            long cents = dollars.movePointRight(2).longValueExact();
            if (cents <= 0) throw new IllegalArgumentException("Amount must be positive");
            return cents;
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("Use a positive dollar amount with at most two decimal places", exception);
        }
    }

    public static String format(long cents) {
        if (cents < 0) throw new IllegalArgumentException("Money cannot be negative");
        var format = NumberFormat.getNumberInstance(Locale.US);
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(cents % 100 == 0 ? 0 : 2);
        format.setMaximumFractionDigits(2);
        return "$" + format.format(BigDecimal.valueOf(cents, 2));
    }
}
