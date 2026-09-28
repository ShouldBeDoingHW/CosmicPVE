package com.cosmicpve.trial;

import com.cosmicpve.content.definition.trial.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import java.nio.file.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrialRoomDataRefactorTest {
    @Test void allProductionRoomsRetainNativeTiersStructuresStableIdsAndExplicitAdapters() throws Exception {
        Map<TrialRoomCategory,Integer> counts=new EnumMap<>(TrialRoomCategory.class);
        int total=0;
        try(var paths=Files.list(Path.of(System.getProperty("cosmicpve.projectDir"),"src/main/resources/data/cosmicpve/cosmicpve/trial_rooms/trial"))) {
            for(var path:paths.toList()) {
                var data=TrialRoomDefinitionData.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(Files.readString(path))).getOrThrow();
                var id=Identifier.parse("cosmicpve:trial/"+path.getFileName().toString().replace(".json",""));
                var definition=data.resolve(id).valueOrThrow(); total++;
                assertEquals(id,definition.id()); assertFalse(definition.pieces().isEmpty());
                if (TrialSessionService.eligibleNativeTier(definition.category(),TrialPhase.DEMONIC)) {
                    counts.merge(definition.category(),1,Integer::sum);
                    assertEquals(id,definition.handler()); assertEquals(id,definition.loadout());
                    assertEquals(5,definition.baseWeight()); assertTrue(definition.enabled());
                    assertTrue(data.handler().isPresent()); assertTrue(data.loadout().isPresent());
                }
            }
        }
        assertEquals(16,total);
        assertEquals(Map.of(TrialRoomCategory.APPRENTICE,5,TrialRoomCategory.HARDCORE,4,
                TrialRoomCategory.IMPOSSIBLE,3,TrialRoomCategory.DEMONIC,2),counts);
    }
    @Test void nativeTierIntroductionRetainsLowerTiersAndExcludesAdministrativeRooms() {
        for(var phase:TrialPhase.values()) {
            assertTrue(TrialSessionService.eligibleNativeTier(TrialRoomCategory.APPRENTICE,phase));
            assertFalse(TrialSessionService.eligibleNativeTier(TrialRoomCategory.DEVELOPMENT,phase));
            assertFalse(TrialSessionService.eligibleNativeTier(TrialRoomCategory.DECISION,phase));
        }
        assertFalse(TrialSessionService.eligibleNativeTier(TrialRoomCategory.HARDCORE,TrialPhase.APPRENTICE));
        assertTrue(TrialSessionService.eligibleNativeTier(TrialRoomCategory.HARDCORE,TrialPhase.IMPOSSIBLE));
        assertFalse(TrialSessionService.eligibleNativeTier(TrialRoomCategory.DEMONIC,TrialPhase.IMPOSSIBLE));
    }
    @Test void handlerRegistryDetectsDuplicatesAndUnknownsWithoutFallbackMechanics() {
        var registry=new TrialRoomHandlerRegistry(); var id=Identifier.parse("cosmicpve:trial/test");
        assertFalse(registry.contains(id)); assertFalse(registry.hasStarter(id));
        registry.register(id,ignored -> {}); registry.registerStarter(id,(level,session,placement) -> null);
        assertTrue(registry.contains(id)); assertTrue(registry.hasStarter(id));
        assertThrows(IllegalArgumentException.class,() -> registry.register(id,ignored -> {}));
        assertThrows(IllegalArgumentException.class,() -> registry.registerStarter(id,(level,session,placement) -> null));
    }
}
