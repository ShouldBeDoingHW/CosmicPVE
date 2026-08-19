package com.cosmicpve.content.validation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ValidationResult<T>(Optional<T> value, List<ContentDiagnostic> diagnostics) {
    public ValidationResult {
        value = Objects.requireNonNull(value, "value");
        diagnostics = List.copyOf(diagnostics);
        if (value.isPresent() && hasErrors(diagnostics)) {
            throw new IllegalArgumentException("A successful validation result cannot contain errors");
        }
    }

    public static <T> ValidationResult<T> success(T value) {
        return new ValidationResult<>(Optional.of(value), List.of());
    }

    public static <T> ValidationResult<T> success(T value, List<ContentDiagnostic> diagnostics) {
        return new ValidationResult<>(Optional.of(value), diagnostics);
    }

    public static <T> ValidationResult<T> failure(List<ContentDiagnostic> diagnostics) {
        if (!hasErrors(diagnostics)) {
            throw new IllegalArgumentException("A failed validation result requires at least one error");
        }
        return new ValidationResult<>(Optional.empty(), diagnostics);
    }

    public static <T> ValidationResult<T> failure(String source, String message) {
        return failure(List.of(ContentDiagnostic.error(source, message)));
    }

    public boolean isSuccess() {
        return value.isPresent();
    }

    public T valueOrThrow() {
        return value.orElseThrow(() -> new IllegalStateException("Validation failed: " + diagnostics));
    }

    public static boolean hasErrors(List<ContentDiagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR);
    }
}
