package com.cosmicpve.trial;

public record TrialOperationResult(boolean success, String message) {
    public static TrialOperationResult ok(String message) { return new TrialOperationResult(true, message); }
    public static TrialOperationResult rejected(String message) { return new TrialOperationResult(false, message); }
}
