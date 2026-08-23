package com.cosmicpve.content.definition.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class TrialRoomDefinitionTest {
    @Test void twoPieceRoomDecodesAndResolves() {
        var data=TrialRoomDefinitionData.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("""
          {"display_name":"Two Piece Test","category":"development","pieces":[
            {"structure":"cosmicpve:trial/deadeye_west"},
            {"structure":"cosmicpve:trial/deadeye_east","offset":[41,0,0],"rotation":"clockwise_90"}],
           "spawn_marker":"emerald_block","bounds":{"min":[0,0,0],"max":[70,30,42]}}
          """)).getOrThrow();
        var room=data.resolve(Identifier.parse("cosmicpve:trial/two_piece")).valueOrThrow();
        assertEquals(2,room.pieces().size()); assertEquals(41,room.pieces().get(1).offset().getX());
        assertEquals(TrialStructureRotation.CLOCKWISE_90,room.pieces().get(1).rotation());
        assertEquals(70,room.bounds().max().getX());
    }
    @Test void emptyPiecesAndInvertedBoundsReject() {
        var data=new TrialRoomDefinitionData("Bad",TrialRoomCategory.DEVELOPMENT,java.util.List.of(),
                TrialSpawnMarkerRule.EMERALD_BLOCK,new TrialRoomBounds(new net.minecraft.core.BlockPos(2,0,0),net.minecraft.core.BlockPos.ZERO));
        assertFalse(data.resolve(Identifier.parse("cosmicpve:bad")).isSuccess());
    }
}
