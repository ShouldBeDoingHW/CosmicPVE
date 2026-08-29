package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.HiddenGraveyardKeyData;
import com.cosmicpve.trial.TrialRoomLoadoutService;
import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HiddenGraveyardLogicTest {
    @Test void keyIdentityIsVersionedSessionAttemptAndSequenceScoped() {
        UUID session=UUID.randomUUID(), attempt=UUID.randomUUID();
        var key=new HiddenGraveyardKeyData(session,attempt,2);
        assertTrue(key.valid());
        var encoded=HiddenGraveyardKeyData.CODEC.encodeStart(JsonOps.INSTANCE,key).getOrThrow();
        assertEquals(key,HiddenGraveyardKeyData.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        assertNotEquals(key,new HiddenGraveyardKeyData(session,UUID.randomUUID(),2));
        assertThrows(IllegalArgumentException.class,()->new HiddenGraveyardKeyData(session,attempt,4));
    }

    @Test void everyWaveUsesSixDisjointExactTenBlockGraves() {
        for(int wave=1;wave<=3;wave++) {
            var groups=HiddenGraveyardService.authoredGraves(wave);
            assertEquals(6,groups.size());
            assertTrue(groups.stream().allMatch(group->group.size()==10));
            var unique=new HashSet<>(groups.stream().flatMap(java.util.Collection::stream).toList());
            assertEquals(60,unique.size());
        }
    }

    @Test void waveConstantsPreserveOverlapAndHalfKillKeyThreshold() {
        assertEquals(3,HiddenGraveyardService.WAVE_COUNT);
        assertEquals(6,HiddenGraveyardService.CORPSES_PER_WAVE);
        assertEquals(3,HiddenGraveyardService.KEY_KILL_THRESHOLD);
        assertEquals(12,HiddenGraveyardService.CORPSES_PER_WAVE*2);
        assertFalse(HiddenGraveyardService.shouldIssueNextKey(1,2));
        assertTrue(HiddenGraveyardService.shouldIssueNextKey(1,3));
        assertTrue(HiddenGraveyardService.shouldIssueNextKey(2,3));
        assertFalse(HiddenGraveyardService.shouldIssueNextKey(3,3));
        assertFalse(HiddenGraveyardService.completionReady(2,0));
        assertFalse(HiddenGraveyardService.completionReady(3,1));
        assertTrue(HiddenGraveyardService.completionReady(3,0));
    }

    @Test void waveProfilesMatchCanonicalArmorAndIndependentRanges() {
        var one=HiddenGraveyardService.waveProfile(1);
        assertEquals(HiddenGraveyardService.ArmorTier.LEATHER,one.armorTier()); assertEquals(0,one.protectionLevel());
        var two=HiddenGraveyardService.waveProfile(2);
        assertEquals(HiddenGraveyardService.ArmorTier.CHAINMAIL,two.armorTier()); assertEquals(1,two.protectionLevel());
        assertEquals(1,two.luckMinimum()); assertEquals(10,two.luckMaximum());
        var three=HiddenGraveyardService.waveProfile(3);
        assertEquals(HiddenGraveyardService.ArmorTier.IRON,three.armorTier()); assertEquals(2,three.protectionLevel());
        assertEquals(5,three.luckMinimum()); assertEquals(10,three.luckMaximum());
        assertEquals(1,three.aegisMinimum()); assertEquals(6,three.aegisMaximum());
    }

    @Test void graveDoubleChanceUsesFixedRoomEntryPartySizeFormula() {
        assertEquals(0.0D, HiddenGraveyardService.doubleCorpseChance(1));
        assertEquals(0.2D, HiddenGraveyardService.doubleCorpseChance(2));
        assertEquals(0.4D, HiddenGraveyardService.doubleCorpseChance(3));
        assertEquals(0.6D, HiddenGraveyardService.doubleCorpseChance(4), 1.0E-9D);
        assertThrows(IllegalArgumentException.class, () -> HiddenGraveyardService.doubleCorpseChance(0));
    }

    @Test void temporaryPlayerLoadoutConstantsMatchCanonicalRoomDesign() {
        assertEquals(4,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_PROTECTION);
        assertEquals(3,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_UNBREAKING);
        assertEquals(3,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_ENDER_SHIFT);
        assertEquals(3,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_NUTRITION);
        assertEquals(8,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_INSANITY);
        assertEquals(3,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_PUMMEL);
        assertEquals(5,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_APPLES);
        assertEquals(4,TrialRoomLoadoutService.HIDDEN_GRAVEYARD_HEALING_POTIONS);
    }
}
