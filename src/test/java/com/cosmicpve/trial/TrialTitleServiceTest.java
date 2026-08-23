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
    @Test void decisionUpdatesNeverInventARoomStartPresentation() {
        var service=new TrialTitleService(); UUID player=UUID.randomUUID(); int count=0;
        for(int seconds=30;seconds>=26;seconds--) if(service.acceptPresentation(player,"decision:false:"+seconds)) count++;
        assertEquals(5,count); assertFalse(service.acceptPresentation(player,"decision:false:26"));
    }
}
