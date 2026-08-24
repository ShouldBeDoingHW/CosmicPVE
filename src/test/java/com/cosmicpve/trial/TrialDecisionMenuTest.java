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
    @Test void paginationUsesEighteenOrderedEntriesPerPage() {
        assertEquals(1,TrialDecisionMenu.totalPages(0)); assertEquals(1,TrialDecisionMenu.totalPages(18));
        assertEquals(2,TrialDecisionMenu.totalPages(19)); assertEquals(2,TrialDecisionMenu.totalPages(36));
        assertEquals(3,TrialDecisionMenu.totalPages(37));
        assertEquals(0,TrialDecisionMenu.pagePotIndex(0,9)); assertEquals(17,TrialDecisionMenu.pagePotIndex(0,26));
        assertEquals(18,TrialDecisionMenu.pagePotIndex(1,9)); assertEquals(35,TrialDecisionMenu.pagePotIndex(1,26));
    }
}
