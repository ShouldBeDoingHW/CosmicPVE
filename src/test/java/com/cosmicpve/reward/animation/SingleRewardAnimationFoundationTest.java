package com.cosmicpve.reward.animation;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SingleRewardAnimationFoundationTest {
    @Test void timelineUsesTwentyPreviewUpdatesAndExactCountdownBoundaries() {
        var updates = java.util.stream.IntStream.rangeClosed(0, LootAnimationTimeline.REVEAL_TICK)
                .filter(LootAnimationTimeline::previewDue).boxed().toList();
        assertEquals(20, updates.size());
        assertEquals(0, updates.getFirst()); assertEquals(95, updates.getLast());
        assertEquals(5, LootAnimationTimeline.countdown(0));
        assertEquals(4, LootAnimationTimeline.countdown(20));
        assertEquals(3, LootAnimationTimeline.countdown(40));
        assertEquals(2, LootAnimationTimeline.countdown(60));
        assertEquals(1, LootAnimationTimeline.countdown(80));
        assertEquals(0, LootAnimationTimeline.countdown(100));
        assertEquals(160, LootAnimationTimeline.CLOSE_TICK);
    }

    @Test void previewAudioFiresTwicePerWindowAndRepeatsExactEightPitchPattern() {
        var sounds = java.util.stream.IntStream.range(0, LootAnimationTimeline.REVEAL_TICK)
                .filter(LootAnimationTimeline::previewSoundDue).boxed().toList();
        assertEquals(40, sounds.size());
        assertEquals(List.of(0, 2, 5, 7, 10, 12), sounds.subList(0, 6));
        var expected = List.of(1.0F, 0.8F, 0.5F, 0.9F, 1.1F, 1.3F, 1.5F, 1.2F,
                1.0F, 0.8F, 0.5F, 0.9F, 1.1F, 1.3F, 1.5F, 1.2F);
        for (int ordinal = 0; ordinal < expected.size(); ordinal++)
            assertEquals(expected.get(ordinal), LootAnimationTimeline.previewPitch(ordinal), 0.0001F);
        for (int ordinal = 0; ordinal < 40; ordinal++)
            assertEquals(expected.get(ordinal % 8), LootAnimationTimeline.previewPitch(ordinal), 0.0001F);
        assertEquals(0.1F, LootAnimationFeedback.REVEAL_PITCH);
    }

    @Test void arbitraryAndWeightedPreviewProvidersReturnCopies() {
        var uniform = LootAnimationPreviewProvider.uniform(List.of(new ItemStack(Items.BRICK)));
        ItemStack first = uniform.next(RandomSource.create(1));
        first.setCount(9);
        assertEquals(1, uniform.next(RandomSource.create(1)).getCount());
        var weighted = LootAnimationPreviewProvider.weighted(List.of(
                new LootAnimationPreviewProvider.WeightedPreview(new ItemStack(Items.FEATHER), 1),
                new LootAnimationPreviewProvider.WeightedPreview(new ItemStack(Items.COPPER_INGOT), 3)));
        assertFalse(weighted.next(RandomSource.create(2)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> LootAnimationPreviewProvider.weighted(List.of()));
    }

    @Test void pendingRewardRoundTripsAsARealItemStack() {
        var pending = PendingLootAnimation.of(new ItemStack(Items.AMETHYST_SHARD, 3));
        var encoded = PendingLootAnimation.CODEC.codec().encodeStart(JsonOps.INSTANCE, pending).getOrThrow();
        var decoded = PendingLootAnimation.CODEC.codec().parse(JsonOps.INSTANCE,
                JsonParser.parseString(encoded.toString())).getOrThrow();
        assertTrue(decoded.valid());
        assertEquals(Items.AMETHYST_SHARD, decoded.reward().orElseThrow().getItem());
        assertEquals(3, decoded.reward().orElseThrow().getCount());
    }

    @Test void pendingThreeRewardPayloadRoundTripsAtomically() {
        var pending = PendingLootAnimation.of(List.of(new ItemStack(Items.BRICK, 2),
                new ItemStack(Items.FEATHER, 3), new ItemStack(Items.AMETHYST_SHARD, 4)));
        var encoded = PendingLootAnimation.CODEC.codec().encodeStart(JsonOps.INSTANCE, pending).getOrThrow();
        var decoded = PendingLootAnimation.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(decoded.valid());
        assertEquals(3, decoded.allRewards().size());
        assertEquals(List.of(2, 3, 4), decoded.allRewards().stream().map(ItemStack::getCount).toList());
        assertTrue(decoded.reward().isEmpty());
    }
}
