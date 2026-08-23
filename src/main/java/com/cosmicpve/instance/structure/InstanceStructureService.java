package com.cosmicpve.instance.structure;

import com.cosmicpve.content.definition.trial.TrialRoomDefinition;
import com.cosmicpve.instance.InstanceBounds;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

public final class InstanceStructureService {
    public InstanceStructurePlacement place(ServerLevel level, TrialRoomDefinition definition, BlockPos origin) {
        BoundingBox combined = null;
        var markers = new ArrayList<BlockPos>();
        for (var piece : definition.pieces()) {
            var template = level.getStructureManager().get(piece.structure()).orElseThrow(
                    () -> new IllegalStateException("Missing Trial structure " + piece.structure()));
            BlockPos pieceOrigin = origin.offset(piece.offset());
            var settings = new StructurePlaceSettings().setRotation(piece.rotation().minecraft());
            BoundingBox pieceBounds = template.getBoundingBox(settings, pieceOrigin);
            if (!template.placeInWorld(level, pieceOrigin, pieceOrigin, settings, RandomSource.create(), 2)) {
                throw new IllegalStateException("Could not place Trial structure " + piece.structure());
            }
            markers.addAll(template.filterBlocks(pieceOrigin, settings, Blocks.EMERALD_BLOCK).stream()
                    .map(info -> info.pos()).toList());
            if (combined == null) combined = pieceBounds;
            else combined.encapsulate(pieceBounds);
        }
        if (markers.size() != 1) {
            throw new IllegalStateException("Trial room " + definition.id() + " requires exactly one Emerald Block spawn marker, found " + markers.size());
        }
        BlockPos marker = markers.getFirst();
        level.setBlock(marker, Blocks.AIR.defaultBlockState(), 3);
        InstanceBounds declared = InstanceBounds.from(definition.bounds().at(origin));
        return new InstanceStructurePlacement(declared, marker.above());
    }

    public void cleanup(ServerLevel level, InstanceBounds bounds) {
        AABB area = new AABB(bounds.min().getX(), bounds.min().getY(), bounds.min().getZ(),
                bounds.max().getX() + 1.0, bounds.max().getY() + 1.0, bounds.max().getZ() + 1.0);
        level.getEntities((Entity)null, area, entity -> !(entity instanceof Player)).forEach(Entity::discard);
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
