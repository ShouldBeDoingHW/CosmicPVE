package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TrialProgressTest {
    @Test void rewardsRemainOrderedAndIdenticalResultsStayDistinct() {
        var first = new ItemStack(Items.APPLE);
        var progress = TrialProgress.EMPTY.completeRoom(List.of(first)).completeRoom(List.of(first));
        assertEquals(2, progress.pot().size());
        assertNotEquals(progress.pot().get(0).acquisitionId(), progress.pot().get(1).acquisitionId());
        assertTrue(ItemStack.isSameItemSameComponents(progress.pot().get(0).items().getFirst(), first));
        assertTrue(ItemStack.isSameItemSameComponents(progress.pot().get(1).items().getFirst(), first));
    }

    @Test void fourthApprenticeCompletionCrossesBoundaryExactlyOnce() {
        var progress = TrialProgress.EMPTY;
        for (int i = 0; i < 3; i++) progress = progress.completeRoom(List.of(new ItemStack(Items.APPLE)));
        assertEquals(TrialPhase.APPRENTICE, progress.phase());
        assertFalse(progress.hardcoreBonusApplied());
        var beforeBoundary=progress;
        progress = progress.completeRoom(List.of(new ItemStack(Items.CARROT)));
        assertEquals(4, progress.completedRooms());
        assertEquals(TrialPhase.HARDCORE, progress.phase());
        assertTrue(progress.hardcoreBonusApplied());
        assertEquals(4_200,TrialSessionService.completionTimeBonus(beforeBoundary,progress));
        assertEquals(300,TrialSessionService.completionTimeBonus(progress,
                progress.completeRoom(List.of(new ItemStack(Items.POTATO)))));
        assertEquals(5, progress.completeRoom(List.of(new ItemStack(Items.POTATO))).completedRooms());
    }

    @Test void fourthHardcoreCompletionCrossesDemonicBoundaryExactlyOnce() {
        var progress = TrialProgress.EMPTY;
        for (int i=0;i<4;i++) progress=progress.completeRoom(List.of(new ItemStack(Items.APPLE)));
        for (int i=0;i<3;i++) progress=progress.completeRoom(List.of(new ItemStack(Items.CARROT)));
        assertEquals(7,progress.completedRooms()); assertEquals(TrialPhase.HARDCORE,progress.phase());
        var before=progress; var after=progress.completeRoom(List.of(new ItemStack(Items.POTATO)));
        assertEquals(8,after.completedRooms()); assertEquals(TrialPhase.DEMONIC,after.phase());
        assertTrue(after.demonicBonusApplied()); assertEquals(3_900,TrialSessionService.completionTimeBonus(before,after));
        assertEquals(0,TrialSessionService.completionTimeBonus(after,
                after.completeRoom(List.of(new ItemStack(Items.BEETROOT)))));
    }

    @Test void ordinaryHardcoreCompletionAddsOnePotEntryAndExactlyThreeHundredTicks() {
        var before=TrialProgress.EMPTY.debugSetCompletedRooms(4);
        var after=before.completeRoom(List.of(new ItemStack(Items.DIAMOND)));
        assertEquals(1,after.pot().size());
        assertEquals(5,after.completedRooms());
        assertEquals(300,TrialSessionService.completionTimeBonus(before,after));
    }

    @Test void decisionsAreIndividualAndAppearancesPersist() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        Identifier room = Identifier.parse("cosmicpve:trial/raiding_rainbow");
        var progress = TrialProgress.EMPTY.beginDecision(List.of(a, b)).decide(a, TrialDecision.DEAL)
                .beginRoom(room, TrialEncounterState.EMPTY);
        assertEquals(1, progress.appearances(room));
        assertTrue(progress.decisions().isEmpty());
    }
}
