package com.cosmicpve.content.definition.trial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;

public enum TrialStructureRotation implements StringRepresentable {
    NONE("none", Rotation.NONE), CLOCKWISE_90("clockwise_90", Rotation.CLOCKWISE_90),
    CLOCKWISE_180("clockwise_180", Rotation.CLOCKWISE_180),
    COUNTERCLOCKWISE_90("counterclockwise_90", Rotation.COUNTERCLOCKWISE_90);

    public static final Codec<TrialStructureRotation> CODEC =
            StringRepresentable.fromEnum(TrialStructureRotation::values);
    private final String serializedName;
    private final Rotation minecraft;
    TrialStructureRotation(String serializedName, Rotation minecraft) {
        this.serializedName = serializedName;
        this.minecraft = minecraft;
    }
    @Override public String getSerializedName() { return serializedName; }
    public Rotation minecraft() { return minecraft; }
}
