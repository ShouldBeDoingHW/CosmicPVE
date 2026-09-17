package com.cosmicpve.trial;

import com.cosmicpve.trial.trinket.*;
import com.cosmicpve.data.component.*;
import com.cosmicpve.registry.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MadnessPolishTest {
    @Test void allTenDescriptionsAreRequiredReadableAndHideCadence() throws Exception {
        var definitions=MadnessStateTest.definitions(); assertEquals(10,definitions.size());
        for(var d:definitions) {
            assertTrue(d.valid()); assertFalse(d.name().isBlank()); assertFalse(d.description().isBlank());
            assertTrue(com.cosmicpve.trial.madness.MadnessDefinition.HANDLERS.contains(d.handler().getPath()));
            if(d.parameters().containsKey("interval_ticks") || d.handler().getPath().equals("time_glitch"))
                assertTrue(d.description().toLowerCase().contains("every so often"));
            assertFalse(d.description().matches(".*(?:20 seconds|25 seconds|30 seconds|40 seconds|15th second|interval_ticks).*"));
        }
        var missing=JsonParser.parseString("{\"id\":\"cosmicpve:bad\",\"name\":\"Bad\",\"icon\":\"minecraft:stone\",\"handler\":\"cosmicpve:wet_noodle\"}");
        assertTrue(com.cosmicpve.trial.madness.MadnessDefinition.CODEC.parse(JsonOps.INSTANCE,missing).error().isPresent());
    }
    @Test void physicalTrinketsApplyConsumePersistAndReplaceRatherThanAdd() {
        var application=new TrialTrinketApplicationService();
        for(int value=1;value<=3;value++) {
            var trinket=TrialTrinkets.create(TrialTrinketType.MADNESS,value,2); var portal=new ItemStack(ModItems.TRIAL_PORTAL.get());
            assertEquals(TrialTrinketApplicationService.Outcome.SUCCESS,application.apply(trinket,portal,portal));
            assertEquals(1,trinket.getCount());
            var modifiers=portal.get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get());
            assertEquals(value,modifiers.madnessOptionBonus()); assertEquals(2+value,modifiers.madnessChoices(10));
            assertEquals(modifiers,portal.copy().get(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()));
            assertEquals(modifiers,TrialPortalModifiers.CODEC.parse(JsonOps.INSTANCE,TrialPortalModifiers.CODEC.encodeStart(JsonOps.INSTANCE,modifiers).getOrThrow()).getOrThrow());
            assertEquals(TrialTrinketApplicationService.Outcome.REJECTED_EQUAL_OR_WEAKER,application.apply(trinket,portal,portal));
            assertEquals(1,trinket.getCount());
            assertEquals(0x8C1708,trinket.getHoverName().getStyle().getColor().getValue());
        }
        var one=TrialPortalModifiers.EMPTY.with(new TrialTrinketData(TrialTrinketType.MADNESS,1));
        assertEquals(2,one.with(new TrialTrinketData(TrialTrinketType.MADNESS,2)).madnessOptionBonus());
        assertEquals(5,new TrialPortalModifiers(3,0,0,0,0,1000).madnessChoices(10));
        assertEquals(1,one.madnessChoices(1));
    }
    @Test void exactRewardRowsHaveOnlyIntendedTierWeightsAndQuantities() {
        for(var catalog:List.of(TrialRewardCatalogs.APPRENTICE,TrialRewardCatalogs.HARDCORE,TrialRewardCatalogs.IMPOSSIBLE,TrialRewardCatalogs.DEMONIC)) {
            var actual=catalog.declared().stream().filter(row -> row.name().contains("Madness Ballot")).toList();
            var expected=catalog==TrialRewardCatalogs.HARDCORE ? List.of(new TrialRewardCatalogs.Row("+1 Madness Ballot Trial Trinket",10,1,true),new TrialRewardCatalogs.Row("+2 Madness Ballot Trial Trinket",3,1,true))
                    : catalog==TrialRewardCatalogs.IMPOSSIBLE ? List.of(new TrialRewardCatalogs.Row("+2 Madness Ballot Trial Trinket",8,1,true))
                    : catalog==TrialRewardCatalogs.DEMONIC ? List.of(new TrialRewardCatalogs.Row("+3 Madness Ballot Trial Trinket",8,1,true)) : List.of();
            assertEquals(expected,actual);
        }
    }
    @Test void actualTimeAndNativeObfuscationAreIndependentOfRoomAndDecisionTimers() throws Exception {
        var glitch=MadnessStateTest.definitions().stream().filter(d -> d.handler().getPath().equals("time_glitch")).findFirst().orElseThrow();
        var session=TrialSession.joining(UUID.randomUUID(),net.minecraft.resources.Identifier.parse("minecraft:overworld"),net.minecraft.core.BlockPos.ZERO,List.of(),List.of())
                .withProgress(TrialProgress.EMPTY.withMadness(new com.cosmicpve.trial.madness.MadnessState(0,List.of(glitch),List.of(),List.of())));
        for(var lifecycle:List.of(TrialLifecycleState.DECISION,TrialLifecycleState.ROOM_ACTIVE)) {
            var state=session.withState(lifecycle,345,java.util.Optional.empty(),false,List.of());
            for(int tick:new int[]{0,20,300}) {
                boolean hidden=!com.cosmicpve.trial.madness.MadnessRuntime.timerReadable(state,tick);
                var component=TrialTimerDisplayService.timerComponent(TrialTimerDisplayService.displayedSeconds(state.timerTicks()),hidden);
                assertEquals("10m 00s",component.getString()); assertEquals(tick==20,component.getStyle().isObfuscated());
                assertEquals(12000,state.timerTicks()); assertEquals(345,state.stateTicksRemaining());
            }
        }
        var display=new TrialTimerDisplayService(); UUID player=UUID.randomUUID();
        assertTrue(display.accept(player,600,false)); assertFalse(display.accept(player,600,false));
        assertTrue(display.accept(player,600,true)); assertFalse(display.accept(player,600,true));
        assertTrue(display.accept(player,600,false));
    }
}
