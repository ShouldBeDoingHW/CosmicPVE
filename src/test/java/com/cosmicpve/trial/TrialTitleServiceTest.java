package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialTitleServiceTest {
    @Test void roomTitleIsCanonicalOrangeAndCountdownSequenceIsExact() {
        assertEquals(0xFFAA00, TrialTitleService.roomTitle("Room").getStyle().getColor().getValue());
        for(int second=5;second>=1;second--) assertTrue(TrialTitleService.roomSubtitle(second).getString().endsWith(second+"s"));
    }
    @Test void decisionCopyMatchesLifecycleContext() {
        assertEquals("Decision Box",TrialTitleService.decisionTitle().getString());
        assertEquals("30 seconds for players to join!",TrialTitleService.decisionSubtitle(true,30).getString());
        assertEquals("30 seconds to choose!",TrialTitleService.decisionSubtitle(false,30).getString());
    }
}
