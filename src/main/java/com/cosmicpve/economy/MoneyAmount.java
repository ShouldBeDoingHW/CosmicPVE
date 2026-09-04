package com.cosmicpve.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MoneyAmount {
    private static final Pattern PLAIN = Pattern.compile("(?:0|[1-9]\\d*)(?:\\.\\d+)?");
    private static final Pattern GROUPED = Pattern.compile("[1-9]\\d{0,2}(?:,\\d{3})+(?:\\.\\d+)?");

    private MoneyAmount() {}

    public static long parseCents(String input) {
        if (input == null) throw new IllegalArgumentException("Amount is required");
        String normalized = input.trim();
        if (normalized.startsWith("$")) normalized = normalized.substring(1);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Amount is required");
        char suffix = Character.toLowerCase(normalized.charAt(normalized.length() - 1));
        BigDecimal multiplier = BigDecimal.ONE;
        if (suffix == 'k' || suffix == 'm') {
            normalized = normalized.substring(0, normalized.length() - 1);
            multiplier = suffix == 'k' ? BigDecimal.valueOf(1_000L) : BigDecimal.valueOf(1_000_000L);
        }
        if (!(PLAIN.matcher(normalized).matches() || GROUPED.matcher(normalized).matches())) {
            throw new IllegalArgumentException("Use conventional comma grouping and only k/m shorthand");
        }
        normalized = normalized.replace(",", "");
        try {
            BigDecimal dollars = new BigDecimal(normalized).multiply(multiplier).setScale(2, RoundingMode.UNNECESSARY);
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
