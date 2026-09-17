package com.cosmicpve.trial;

import com.cosmicpve.trial.madness.*;
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MadnessStateTest {
    static List<MadnessDefinition> definitions() throws Exception {
        try (var paths = Files.list(Path.of(System.getProperty("cosmicpve.projectDir"),"src/main/resources/data/cosmicpve/cosmicpve/madness"))) {
            return paths.sorted().map(path -> {
                try { return MadnessDefinition.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(Files.readString(path))).getOrThrow(); }
                catch (Exception exception) { throw new RuntimeException(exception); }
            }).toList();
        }
    }
    @Test void allEightDataDefinitionsResolveAndHaveCanonicalIntervals() throws Exception {
        var definitions = definitions(); assertEquals(10,definitions.size());
        assertEquals(MadnessDefinition.HANDLERS,definitions.stream().map(d -> d.handler().getPath()).collect(java.util.stream.Collectors.toSet()));
        for (var row : Map.of("inventory_shuffle",360,"owl_gene",400,"statues",600,"rocket_man",500).entrySet())
            assertEquals(row.getValue().intValue(),definitions.stream().filter(d -> d.handler().getPath().equals(row.getKey())).findFirst().orElseThrow().interval(0));
        var breeze=definitions.stream().filter(d -> d.handler().getPath().equals("gentle_breeze")).findFirst().orElseThrow();
        assertEquals(0.004,breeze.parameter("acceleration",0));
        assertEquals(0.15,breeze.parameter("maximum_horizontal_speed",0));
        var oldVelocity=MadnessRuntime.breezeVelocity(net.minecraft.world.phys.Vec3.ZERO,new net.minecraft.world.phys.Vec3(1,0,0),false,0.008,0.15);
        var newVelocity=MadnessRuntime.breezeVelocity(net.minecraft.world.phys.Vec3.ZERO,new net.minecraft.world.phys.Vec3(1,0,0),false,breeze.parameter("acceleration",0),0.15);
        assertEquals(oldVelocity.x*0.5,newVelocity.x);
    }
    @Test void choicesAreDistinctSharedAndExcludeAlreadySelected() throws Exception {
        var random = RandomSource.create(42); var definitions = definitions();
        var offered = MadnessState.EMPTY.queue(2).offer(definitions,5,random);
        assertEquals(5,offered.options().size());
        assertEquals(5,offered.options().stream().map(MadnessDefinition::id).distinct().count());
        var resolved = offered.resolve(List.of(),random);
        assertEquals(1,resolved.pending()); assertEquals(1,resolved.active().size());
        var next = resolved.offer(definitions,5,random);
        assertFalse(next.options().contains(resolved.active().getFirst()));
        assertEquals(0,next.resolve(List.of(),random).pending());
    }
    @Test void votesAreMutablePluralityExcludesDepartedPlayersAndTiesOnlyUseTop() throws Exception {
        var offered = MadnessState.EMPTY.queue(1).offer(definitions(),3,RandomSource.create(1));
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID(),departed=UUID.randomUUID();
        var first=offered.options().get(0); var second=offered.options().get(1); var third=offered.options().get(2);
        var votes = offered.vote(a,third.id()).vote(a,first.id()).vote(b,first.id()).vote(c,second.id()).vote(departed,third.id());
        assertEquals(4,votes.votes().size());
        assertEquals(first,votes.resolve(List.of(a,b,c),RandomSource.create(9)).active().getFirst());
        var tie = offered.vote(a,first.id()).vote(b,second.id()).vote(departed,third.id());
        for (int seed=0;seed<100;seed++) assertNotEquals(third,tie.resolve(List.of(a,b),RandomSource.create(seed)).active().getFirst());
    }
    @Test void exhaustedPoolConsumesPendingAndDoesNotReactivate() throws Exception {
        var definitions = definitions(); var state = new MadnessState(2,definitions,List.of(),List.of());
        state=state.offer(definitions,5,RandomSource.create(1)); assertTrue(state.options().isEmpty());
        state=state.resolve(List.of(),RandomSource.create(1)).resolve(List.of(),RandomSource.create(2));
        assertEquals(0,state.pending()); assertEquals(10,state.active().size());
    }
    @Test void durableCodecRetainsPendingBallotVotesAndDefinitionSnapshots() throws Exception {
        var state=MadnessState.EMPTY.queue(2).offer(definitions(),5,RandomSource.create(42));
        state=state.vote(UUID.randomUUID(),state.options().getFirst().id());
        var json=MadnessState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        assertEquals(state,MadnessState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
        var progress=TrialProgress.EMPTY.withMadness(state).beginDecision(List.of()).withEncounter(TrialEncounterState.EMPTY);
        assertEquals(state,progress.madness());
    }
    @Test void timeGlitchUsesServerSecondsNotActualTimerAndIncludesDecision() throws Exception {
        var glitch=definitions().stream().filter(d -> d.handler().getPath().equals("time_glitch")).findFirst().orElseThrow();
        var progress=TrialProgress.EMPTY.withMadness(new MadnessState(0,List.of(glitch),List.of(),List.of()));
        var session=TrialSession.joining(UUID.randomUUID(),Identifier.parse("minecraft:overworld"),net.minecraft.core.BlockPos.ZERO,
                List.of(),List.of()).withProgress(progress);
        assertTrue(MadnessRuntime.timerReadable(session,0));
        assertTrue(MadnessRuntime.timerReadable(session,19));
        assertFalse(MadnessRuntime.timerReadable(session,20));
        assertTrue(MadnessRuntime.timerReadable(session,300));
        assertEquals(12000,session.timerTicks());
        var hidden=TrialTimerDisplayService.timerComponent(600,true);
        assertEquals("10m 00s",hidden.getString()); assertTrue(hidden.getStyle().isObfuscated());
    }
    @Test void malformedHandlerAndNonFiniteValuesRejected() {
        var json=JsonParser.parseString("{\"id\":\"cosmicpve:bad\",\"name\":\"Bad\",\"icon\":\"minecraft:stone\",\"handler\":\"cosmicpve:unknown\"}");
        assertTrue(MadnessDefinition.CODEC.parse(JsonOps.INSTANCE,json).error().isPresent());
    }
    @Test void presetsAreNumericConcreteSnapshotsAndSnapshotPublicationRetainsThem() throws Exception {
        var builder=new com.cosmicpve.content.ContentCandidateBuilder();
        try (var paths=Files.list(Path.of(System.getProperty("cosmicpve.projectDir"),"src/main/resources/data/cosmicpve/cosmicpve/trial_portal_presets"))) {
            for (var path:paths.toList()) {
                var modifiers=TrialPortalModifiers.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(Files.readString(path))).getOrThrow();
                builder.addTrialPortalPreset(Identifier.parse("cosmicpve:"+path.getFileName().toString().replace(".json","")),modifiers);
            }
        }
        var snapshot=builder.build().valueOrThrow().withRevision(3);
        assertEquals(5,snapshot.trialPortalPresets().size());
        assertEquals(10,snapshot.trialPortalPresets().get(Identifier.parse("cosmicpve:skip_10")).skipRooms());
        assertEquals(200,snapshot.trialPortalPresets().get(Identifier.parse("cosmicpve:fame_200")).famePercent());
        assertEquals(5,snapshot.trialPortalPresets().get(Identifier.parse("cosmicpve:combined")).insuranceItems());
    }
}
