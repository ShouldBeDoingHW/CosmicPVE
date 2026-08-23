package com.cosmicpve.entity.spacepirate;

public record SpacePirateDefinition(float health, float baseWidth, float baseHeight, Presentation presentation) {
    public enum Presentation { ZOMBIFIED_PIGLIN, WITHER_SKELETON }
    public static final float SCALE = 1.35F;
    public static final double MOVEMENT_SPEED = 0.35;
    public static final SpacePirateDefinition VARIANT_1 = new SpacePirateDefinition(
            25.0F, 0.6F, 1.95F, Presentation.ZOMBIFIED_PIGLIN);
    public static final SpacePirateDefinition VARIANT_2 = new SpacePirateDefinition(
            35.0F, 0.7F, 2.4F, Presentation.WITHER_SKELETON);
    public float width() { return baseWidth * SCALE; }
    public float height() { return baseHeight * SCALE; }
}
