package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.junit.jupiter.api.Test;

class UnexaminedRewardDeliveryTest {
    @Test void openingStackOfTwoLeavesOneSourceAndDeliversOneRewardOnce() {
        ItemStack source = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get(), 2);
        ItemStack reward = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        List<ItemStack> insertedOrDropped = new ArrayList<>();

        ItemStack heldAfter = UnexaminedRewardDelivery.deliver(source, reward, insertedOrDropped::add);

        assertSame(source, heldAfter);
        assertEquals(1, heldAfter.getCount());
        assertEquals(List.of(reward), insertedOrDropped);
    }

    @Test void openingFinalSourceTransformsHandDirectlyIntoReward() {
        ItemStack source = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get());
        ItemStack reward = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        List<ItemStack> insertedOrDropped = new ArrayList<>();

        ItemStack heldAfter = UnexaminedRewardDelivery.deliver(source, reward, insertedOrDropped::add);

        assertTrue(source.isEmpty());
        assertSame(reward, heldAfter);
        assertTrue(insertedOrDropped.isEmpty());
    }

    @Test void repeatedOpeningThroughFinalSourceAwardsExactlyOneRewardPerConsumedBook() {
        ItemStack source = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get(), 5);
        List<ItemStack> delivered = new ArrayList<>();

        for (int opening = 0; opening < 5; opening++) {
            ItemStack reward = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
            ItemStack heldAfter = UnexaminedRewardDelivery.deliver(source, reward, delivered::add);
            if (heldAfter == reward) delivered.add(heldAfter);
        }

        assertTrue(source.isEmpty());
        assertEquals(5, delivered.size());
        assertTrue(delivered.stream().allMatch(stack -> stack.is(ModItems.COSMIC_ENCHANTMENT_BOOK.get())));
    }

    @Test void fullInventoryFallbackCanDropExactlyOneRewardWithoutDuplication() {
        ItemStack source = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get(), 3);
        ItemStack reward = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        List<ItemStack> droppedBecauseInventoryIsFull = new ArrayList<>();

        ItemStack heldAfter = UnexaminedRewardDelivery.deliver(
                source, reward, droppedBecauseInventoryIsFull::add);

        assertSame(source, heldAfter);
        assertEquals(2, heldAfter.getCount());
        assertEquals(1, droppedBecauseInventoryIsFull.size());
        assertSame(reward, droppedBecauseInventoryIsFull.getFirst());
    }

    @Test void finalStackDeliveryDoesNotRerollOrDuplicateTheOpeningResult() {
        var random = new CountingRandomSource(RandomSource.create(921L));
        var service = new UnexaminedBookOpeningService(new BookOpeningRateService());
        var result = service.roll(CosmicEnchantmentTier.SIMPLE,
                List.of(CosmicEnchantmentSpecs.GLOWING), random, null).orElseThrow();
        int callsAfterSingleRoll = random.boundedCalls;
        ItemStack source = new ItemStack(ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get());
        ItemStack reward = UnexaminedBooks.revealed(result);
        List<ItemStack> fallback = new ArrayList<>();

        ItemStack heldAfter = UnexaminedRewardDelivery.deliver(source, reward, fallback::add);

        assertEquals(4, callsAfterSingleRoll);
        assertEquals(callsAfterSingleRoll, random.boundedCalls);
        assertSame(reward, heldAfter);
        assertTrue(fallback.isEmpty());
    }

    private static final class CountingRandomSource implements RandomSource {
        private final RandomSource delegate;
        private int boundedCalls;

        private CountingRandomSource(RandomSource delegate) {
            this.delegate = delegate;
        }

        @Override public RandomSource fork() { return delegate.fork(); }
        @Override public PositionalRandomFactory forkPositional() { return delegate.forkPositional(); }
        @Override public void setSeed(long seed) { delegate.setSeed(seed); }
        @Override public int nextInt() { return delegate.nextInt(); }
        @Override public int nextInt(int bound) { boundedCalls++; return delegate.nextInt(bound); }
        @Override public long nextLong() { return delegate.nextLong(); }
        @Override public boolean nextBoolean() { return delegate.nextBoolean(); }
        @Override public float nextFloat() { return delegate.nextFloat(); }
        @Override public double nextDouble() { return delegate.nextDouble(); }
        @Override public double nextGaussian() { return delegate.nextGaussian(); }
    }
}
