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
        assertTrue(service.acceptOwner(player,"MrWoofless's Trial"));
        assertFalse(service.acceptOwner(player,"MrWoofless's Trial"));
        assertTrue(service.acceptOwner(player,"Another's Trial"));
    }
    @Test void authoritativePhaseIsAcceptedOnlyWhenItChangesAndUsesCanonicalColors() {
        var service=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(service.acceptPhase(player,TrialPhase.APPRENTICE));
        assertFalse(service.acceptPhase(player,TrialPhase.APPRENTICE));
        assertTrue(service.acceptPhase(player,TrialPhase.HARDCORE));
        assertFalse(service.acceptPhase(player,TrialPhase.HARDCORE));
        assertEquals(0xE6E032,TrialPhase.APPRENTICE.color());
        assertEquals(0xE6A732,TrialPhase.HARDCORE.color());
        assertEquals(0xE65C32,TrialPhase.DEMONIC.color());
    }
}
