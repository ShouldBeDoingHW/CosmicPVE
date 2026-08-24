package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class TrialDecisionEntryServiceTest {
    @Test void decisionEntryTeleportsThenRestoresThenPresentsExactlyOnce() {
        var calls=new ArrayList<String>();
        TrialDecisionEntryService.perform(() -> calls.add("teleport"), () -> calls.add("restore"), () -> calls.add("present"));
        assertEquals(java.util.List.of("teleport","restore","present"),calls);
    }
}
