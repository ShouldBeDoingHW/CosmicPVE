package com.cosmicpve.trial.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TrialSnapshotModelTest {
    @Test void exactStacksAndEmptySlotsRoundTripThroughPersistentCodec() {
        ItemStack sword=new ItemStack(Items.DIAMOND_SWORD); sword.setDamageValue(77);
        sword.set(DataComponents.CUSTOM_NAME,Component.literal("Outside Sword"));
        var main=new ArrayList<ItemStack>(); for(int i=0;i<36;i++) main.add(ItemStack.EMPTY);
        main.set(0,sword); main.set(35,new ItemStack(Items.GOLDEN_APPLE,7));
        var snapshot=new TrialOutsideSnapshot(1,UUID.randomUUID(),UUID.randomUUID(),main,
                List.of(ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(Items.IRON_CHESTPLATE),ItemStack.EMPTY),
                new ItemStack(Items.SHIELD),ItemStack.EMPTY,0,Identifier.parse("minecraft:overworld"),
                12.25,70,-8.75,33.0F,-12.0F);
        var json=TrialOutsideSnapshot.CODEC.encodeStart(JsonOps.INSTANCE,snapshot).getOrThrow();
        var decoded=TrialOutsideSnapshot.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(36,decoded.inventory().size());
        assertEquals(77,decoded.inventory().getFirst().getDamageValue());
        assertEquals("Outside Sword",decoded.inventory().getFirst().getCustomName().getString());
        assertEquals(7,decoded.inventory().getLast().getCount());
        assertTrue(decoded.inventory().get(1).isEmpty()); assertTrue(decoded.offhand().is(Items.SHIELD));
        assertEquals(snapshot.x(),decoded.x()); assertEquals(snapshot.yaw(),decoded.yaw());
    }
    @Test void restoredTombstoneRejectsASecondSnapshotByConstruction() {
        var restored=TrialPlayerState.restored();
        assertEquals(TrialSnapshotPhase.RESTORED,restored.phase()); assertEquals(Optional.empty(),restored.snapshot());
        var json=TrialPlayerState.CODEC.codec().encodeStart(JsonOps.INSTANCE,restored).getOrThrow();
        assertEquals(restored,TrialPlayerState.CODEC.codec().parse(JsonOps.INSTANCE,json).getOrThrow());
    }
    @Test void preparedCashoutRewardsPersistWithoutMutatingTheOutsideSnapshot() {
        var snapshot=new TrialOutsideSnapshot(1,UUID.randomUUID(),UUID.randomUUID(),
                java.util.Collections.nCopies(36,ItemStack.EMPTY),java.util.Collections.nCopies(4,ItemStack.EMPTY),
                ItemStack.EMPTY,ItemStack.EMPTY,0,Identifier.parse("minecraft:overworld"),0,64,0,0,0);
        var state=TrialPlayerState.committed(snapshot).inTrial().withPendingRewards(List.of(new ItemStack(Items.DIAMOND,3)));
        var json=TrialPlayerState.CODEC.codec().encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var decoded=TrialPlayerState.CODEC.codec().parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(3,decoded.pendingRewards().getFirst().getCount());
        assertEquals(snapshot,decoded.snapshot().orElseThrow());
    }
}
