package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TrialTimerDisplayServiceTest {
    @Test void formatsAuthoritativeTicksWithPositiveCeiling() {
        assertEquals("10:00",TrialTimerDisplayService.formatTicks(12_000));
        assertEquals("8:35",TrialTimerDisplayService.formatTicks(10_300));
        assertEquals("0:09",TrialTimerDisplayService.formatTicks(180));
        assertEquals("0:01",TrialTimerDisplayService.formatTicks(1));
        assertEquals("0:00",TrialTimerDisplayService.formatTicks(0));
    }
    @Test void unchangedDisplayedSecondDoesNotRefresh() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.accept(player,600));
        assertFalse(service.accept(player,600));
        assertTrue(service.accept(player,599));
        assertFalse(service.accept(player,599));
    }
    @Test void immutableOwnerHeadingIsAcceptedOnlyWhenItChanges() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.acceptOwner(player,"MrWoofless Trial"));
        assertFalse(service.acceptOwner(player,"MrWoofless Trial"));
        assertTrue(service.acceptOwner(player,"Another Trial"));
    }
}
