package com.cosmicpve.content.validation;

import java.util.Objects;

public record ContentDiagnostic(DiagnosticSeverity severity, String source, String message) {
    public ContentDiagnostic {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(message, "message");
    }

    public static ContentDiagnostic error(String source, String message) {
        return new ContentDiagnostic(DiagnosticSeverity.ERROR, source, message);
    }
}
