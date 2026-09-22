package com.cosmicpve.cosmiccrate;

public enum CosmicCrateSide {
    LEFT("left", "Left"), RIGHT("right", "Right");
    private final String id;
    private final String displayName;
    CosmicCrateSide(String id, String displayName) { this.id = id; this.displayName = displayName; }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public CosmicCrateSide opposite() { return this == LEFT ? RIGHT : LEFT; }
}
