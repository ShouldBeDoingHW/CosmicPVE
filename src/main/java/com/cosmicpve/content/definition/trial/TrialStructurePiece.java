package com.cosmicpve.content.definition.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record TrialStructurePiece(Identifier structure, BlockPos offset, TrialStructureRotation rotation) {
    public static final Codec<TrialStructurePiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("structure").forGetter(TrialStructurePiece::structure),
            BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(TrialStructurePiece::offset),
            TrialStructureRotation.CODEC.optionalFieldOf("rotation", TrialStructureRotation.NONE)
                    .forGetter(TrialStructurePiece::rotation)
    ).apply(instance, TrialStructurePiece::new));
}
