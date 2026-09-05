package com.cosmicpve.activity;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ActivityContextServiceTest {
    @Test void dungeonParkourIsExplicitNarrowAndClearsAcrossActivityChanges() {
        var service=new ActivityContextService();
        var player=UUID.randomUUID();
        assertFalse(service.isDungeonParkour(player));
        service.set(player,ActivityType.DUNGEON);
        assertFalse(service.isDungeonParkour(player));
        service.setDungeonParkour(player,true);
        assertTrue(service.isDungeonParkour(player));
        service.set(player,ActivityType.DUNGEON);
        assertFalse(service.isDungeonParkour(player));
        service.setDungeonParkour(player,true);
        service.set(player,ActivityType.TRIAL);
        assertFalse(service.isDungeonParkour(player));
        service.setDungeonParkour(player,true);
        service.clear(player);
        assertFalse(service.isDungeonParkour(player));
    }
}
