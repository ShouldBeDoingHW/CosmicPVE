package com.cosmicpve.trial.room;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class CaveDivingServiceTest {
    @Test void hotfixUsesFiveTickValidationAndRejectsOnlyWrongInRoomPotPlacements() {
        assertEquals(5, CaveDivingService.VALIDATION_INTERVAL_TICKS);
        var answer = new CaveDivingService.PotAnswer(java.util.List.of(Items.ANGLER_POTTERY_SHERD,
                Items.ARCHER_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD), Direction.NORTH);
        var solution = new net.minecraft.core.BlockPos(9, 9, 9);
        var bounds = new com.cosmicpve.instance.InstanceBounds(net.minecraft.core.BlockPos.ZERO,
                new net.minecraft.core.BlockPos(20, 20, 20));
        var attempt = new CaveDivingService.Attempt(java.util.UUID.randomUUID(), bounds,
                new net.minecraft.core.BlockPos(5, 5, 5), solution, answer, java.util.Map.of(),
                new java.util.LinkedHashSet<>(), false);
        assertTrue(CaveDivingService.rejectsPotPlacement(attempt, new net.minecraft.core.BlockPos(2, 2, 2),
                new net.minecraft.world.item.ItemStack(Items.DECORATED_POT)));
        assertFalse(CaveDivingService.rejectsPotPlacement(attempt, solution,
                new net.minecraft.world.item.ItemStack(Items.DECORATED_POT)));
        assertFalse(CaveDivingService.rejectsPotPlacement(attempt, new net.minecraft.core.BlockPos(21, 2, 2),
                new net.minecraft.world.item.ItemStack(Items.DECORATED_POT)));
        assertFalse(CaveDivingService.rejectsPotPlacement(attempt, new net.minecraft.core.BlockPos(2, 2, 2),
                new net.minecraft.world.item.ItemStack(Items.STONE)));
    }

    @Test void canonicalPoolIsTheSettledNineAndModelAllowsIndependentDuplicates() {
        assertEquals(9, CaveDivingService.CANONICAL_SHERDS.size());
        assertEquals(9, new java.util.HashSet<>(CaveDivingService.CANONICAL_SHERDS).size());
        assertEquals(Items.ANGLER_POTTERY_SHERD, CaveDivingService.CANONICAL_SHERDS.getFirst());
        assertEquals(Items.FRIEND_POTTERY_SHERD, CaveDivingService.CANONICAL_SHERDS.getLast());
        boolean foundDuplicate = false;
        for (int seed=0;seed<100 && !foundDuplicate;seed++) {
            var answer=CaveDivingService.randomAnswer(RandomSource.create(seed));
            foundDuplicate=new java.util.HashSet<>(answer.decorations()).size()<4;
        }
        assertTrue(foundDuplicate);
    }

    @Test void twentyFourUnderwaterSidesGuaranteeTwoCopiesPerModelOccurrence() {
        var model=java.util.List.of(Items.ANGLER_POTTERY_SHERD,Items.ARCHER_POTTERY_SHERD,
                Items.ARCHER_POTTERY_SHERD,Items.BLADE_POTTERY_SHERD);
        var generated=CaveDivingService.generateUnderwaterSherds(model,RandomSource.create(91));
        assertEquals(24,generated.size());
        assertTrue(generated.stream().allMatch(CaveDivingService.CANONICAL_SHERDS::contains));
        var modelCounts=counts(model); var generatedCounts=counts(generated);
        modelCounts.forEach((item,count)->assertTrue(generatedCounts.getOrDefault(item,0)>=count*2));
        for(int pot=0;pot<6;pot++) assertEquals(4,generated.subList(pot*4,pot*4+4).size());
    }

    @Test void exactDecorationsAndOrientationAreRequired() {
        var sides=java.util.List.of(Items.ANGLER_POTTERY_SHERD,Items.ARCHER_POTTERY_SHERD,
                Items.BLADE_POTTERY_SHERD,Items.BREWER_POTTERY_SHERD);
        var model=new CaveDivingService.PotAnswer(sides,Direction.NORTH);
        assertTrue(CaveDivingService.matches(model,new CaveDivingService.PotAnswer(sides,Direction.NORTH)));
        assertFalse(CaveDivingService.matches(model,new CaveDivingService.PotAnswer(sides,Direction.EAST)));
        assertFalse(CaveDivingService.matches(model,new CaveDivingService.PotAnswer(java.util.List.of(
                Items.ARCHER_POTTERY_SHERD,Items.ANGLER_POTTERY_SHERD,
                Items.BLADE_POTTERY_SHERD,Items.BREWER_POTTERY_SHERD),Direction.NORTH)));
        assertFalse(CaveDivingService.matches(model,null));
    }

    @Test void onlyUnderwaterAndSolutionPotsAreBreakableAndUnderwaterSherdsClaimOnce() {
        var answer=new CaveDivingService.PotAnswer(java.util.List.of(Items.ANGLER_POTTERY_SHERD,
                Items.ARCHER_POTTERY_SHERD,Items.BLADE_POTTERY_SHERD,Items.BREWER_POTTERY_SHERD),Direction.NORTH);
        var underwater=new net.minecraft.core.BlockPos(2,3,4); var solution=new net.minecraft.core.BlockPos(9,9,9);
        var attempt=new CaveDivingService.Attempt(java.util.UUID.randomUUID(),
                new com.cosmicpve.instance.InstanceBounds(net.minecraft.core.BlockPos.ZERO,new net.minecraft.core.BlockPos(20,20,20)),
                new net.minecraft.core.BlockPos(5,5,5),solution,answer,java.util.Map.of(underwater,answer),
                new java.util.LinkedHashSet<>(),false);
        assertTrue(CaveDivingService.breakable(attempt,underwater));
        assertTrue(CaveDivingService.breakable(attempt,solution));
        assertFalse(CaveDivingService.breakable(attempt,attempt.modelPosition()));
        assertEquals(answer.decorations(),CaveDivingService.claimSherds(attempt,underwater));
        assertTrue(CaveDivingService.claimSherds(attempt,underwater).isEmpty());
        assertFalse(CaveDivingService.breakable(attempt,underwater));
    }

    @Test void wrongSubmissionRejectsOnceUntilChangedOrRemovedAndCorrectSubmissionSolves() {
        assertEquals(net.minecraft.sounds.SoundEvents.ANVIL_DESTROY,CaveDivingService.REJECTION_SOUND);
        assertEquals(0.8F,CaveDivingService.REJECTION_PITCH);
        var model=new CaveDivingService.PotAnswer(java.util.List.of(Items.ANGLER_POTTERY_SHERD,
                Items.ARCHER_POTTERY_SHERD,Items.BLADE_POTTERY_SHERD,Items.BREWER_POTTERY_SHERD),Direction.NORTH);
        var wrong=new CaveDivingService.PotAnswer(model.decorations(),Direction.EAST);
        var attempt=new CaveDivingService.Attempt(java.util.UUID.randomUUID(),
                new com.cosmicpve.instance.InstanceBounds(net.minecraft.core.BlockPos.ZERO,new net.minecraft.core.BlockPos(20,20,20)),
                new net.minecraft.core.BlockPos(5,5,5),new net.minecraft.core.BlockPos(9,9,9),model,java.util.Map.of(),
                new java.util.LinkedHashSet<>(),false);
        assertEquals(CaveDivingService.ValidationResult.REJECTED,CaveDivingService.validate(attempt,wrong));
        assertEquals(CaveDivingService.ValidationResult.NONE,CaveDivingService.validate(attempt,wrong));
        assertEquals(CaveDivingService.ValidationResult.NONE,CaveDivingService.validate(attempt,null));
        assertEquals(CaveDivingService.ValidationResult.REJECTED,CaveDivingService.validate(attempt,wrong));
        assertEquals(CaveDivingService.ValidationResult.SOLVED,CaveDivingService.validate(attempt,model));
        assertTrue(attempt.solved());
    }

    private static java.util.Map<net.minecraft.world.item.Item,Integer> counts(java.util.List<net.minecraft.world.item.Item> values){
        var counts=new HashMap<net.minecraft.world.item.Item,Integer>(); values.forEach(value->counts.merge(value,1,Integer::sum)); return counts;
    }
}
