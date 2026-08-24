package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import net.minecraft.sounds.SoundEvents;
import java.util.UUID;

class TrialTitleServiceTest {
    @Test void roomTitleIsCanonicalOrangeAndCountdownSequenceIsExact() {
        assertEquals(0xFFAA00, TrialTitleService.roomTitle("Room").getStyle().getColor().getValue());
        for(int second=5;second>=1;second--) assertTrue(TrialTitleService.roomSubtitle(second).getString().endsWith(second+"s"));
    }
    @Test void decisionCopyMatchesLifecycleContext() {
        assertEquals("Decision Box",TrialTitleService.decisionTitle().getString());
        assertEquals("30 seconds for players to join!",TrialTitleService.decisionSubtitle(true,30).getString());
        assertEquals("30 seconds to choose!",TrialTitleService.decisionSubtitle(false,30).getString());
        assertEquals("1 second to choose!",TrialTitleService.decisionSubtitle(false,1).getString());
    }
    @Test void genericCountdownAndRoomStartSoundsArePinned() {
        assertSame(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), TrialTitleService.countdownSound());
        assertSame(SoundEvents.ENDER_DRAGON_GROWL, TrialTitleService.roomStartSound());
    }
    @Test void oneSecondPresentationTokensDispatchExactlyOncePerPlayer() {
        var service=new TrialTitleService(); UUID first=UUID.randomUUID(), second=UUID.randomUUID();
        int firstDispatches=0;
        for(int secondValue=5;secondValue>=1;secondValue--) {
            String token="room:Raiding Rainbow:"+secondValue;
            if(service.acceptPresentation(first,token)) firstDispatches++;
            assertFalse(service.acceptPresentation(first,token),"same-second polling must not replay");
        }
        assertEquals(5,firstDispatches);
        assertTrue(service.acceptPresentation(first,"room_started"));
        assertFalse(service.acceptPresentation(first,"room_started"));
        assertTrue(service.acceptPresentation(second,"room_started"),"each participant receives one targeted event");
    }
    @Test void decisionAnnouncementsUseOnlyCanonicalFlashCadence() {
        var expected=java.util.Set.of(30,25,20,15,10,5,4,3,2,1); int count=0;
        for(int seconds=30;seconds>=0;seconds--) {
            assertEquals(expected.contains(seconds),TrialTitleService.shouldAnnounceDecision(seconds));
            if(TrialTitleService.shouldAnnounceDecision(seconds)) count++;
        }
        assertEquals(10,count);
    }
}
