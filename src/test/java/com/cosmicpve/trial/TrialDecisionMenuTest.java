package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialDecisionMenuTest {
    @Test void layoutHasFourDealCenterBlockFourNoDealAndOrderedPotSlots() {
        for(int i=0;i<4;i++) assertEquals(TrialDecisionMenu.SlotRole.DEAL,TrialDecisionMenu.role(i));
        assertEquals(TrialDecisionMenu.SlotRole.BLOCKED_CENTER,TrialDecisionMenu.role(4));
        for(int i=5;i<9;i++) assertEquals(TrialDecisionMenu.SlotRole.NO_DEAL,TrialDecisionMenu.role(i));
        for(int i=9;i<27;i++) { assertEquals(TrialDecisionMenu.SlotRole.POT,TrialDecisionMenu.role(i)); assertEquals(i-9,TrialDecisionMenu.potIndex(i)); }
    }
}
