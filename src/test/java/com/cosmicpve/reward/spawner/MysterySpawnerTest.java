package com.cosmicpve.reward.spawner;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.MysterySpawnerData;
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.serialization.JsonOps;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.junit.jupiter.api.Test;

class MysterySpawnerTest {
    @Test void typedIdentityRoundTripsAndRejectsStaleOpening() {
        var data = new MysterySpawnerData(1, MysterySpawnerTier.ELITE);
        var encoded = MysterySpawnerData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, MysterySpawnerData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertTrue(MysterySpawners.open(new MysterySpawnerData(0, MysterySpawnerTier.ELITE), bound -> 0).isEmpty());
    }

    @Test void exactPoolsHaveNoCrossTierContamination() {
        assertEquals(6, MysterySpawners.SIMPLE.size());
        assertEquals(8, MysterySpawners.ELITE.size());
        assertEquals(5, MysterySpawners.MASTERY.size());
        assertTrue(java.util.Collections.disjoint(MysterySpawners.SIMPLE, MysterySpawners.ELITE));
        assertTrue(java.util.Collections.disjoint(MysterySpawners.SIMPLE, MysterySpawners.MASTERY));
        assertTrue(java.util.Collections.disjoint(MysterySpawners.ELITE, MysterySpawners.MASTERY));
        assertEquals(Set.of("minecraft:sheep", "minecraft:pig", "minecraft:zombie", "minecraft:spider",
                "minecraft:skeleton", "minecraft:chicken"), strings(MysterySpawners.SIMPLE));
        assertEquals(Set.of("minecraft:cow", "minecraft:creeper", "minecraft:zombified_piglin", "minecraft:husk",
                "minecraft:snow_golem", "minecraft:blaze", "minecraft:slime", "minecraft:enderman"), strings(MysterySpawners.ELITE));
        assertEquals(Set.of("minecraft:iron_golem", "minecraft:wither_skeleton", "minecraft:witch",
                "minecraft:vindicator", "minecraft:guardian"), strings(MysterySpawners.MASTERY));
    }

    @Test void openingDeterministicallyProducesExactlyOneCanonicalTypedSpawner() {
        for (var tier : MysterySpawnerTier.values()) {
            var pool = MysterySpawners.pool(tier);
            for (int index = 0; index < pool.size(); index++) {
                int chosen = index;
                var output = MysterySpawners.open(new MysterySpawnerData(1, tier), bound -> chosen);
                assertEquals(1, output.getCount());
                assertEquals(ModItems.MOB_SPAWNER.get(), output.getItem());
                assertEquals(pool.get(index), output.get(ModDataComponents.MOB_SPAWNER.get()).entityTypeId());
                assertTrue(MobSpawnerEligibility.isEligible(pool.get(index)));
            }
        }
    }

    @Test void itemPresentationAndRequestedOpeningSoundAreCanonical() {
        for (var tier : MysterySpawnerTier.values()) {
            var stack = MysterySpawners.create(tier, 2);
            assertEquals(2, stack.getCount());
            assertEquals(tier, stack.get(ModDataComponents.MYSTERY_SPAWNER.get()).tier());
            assertTrue(stack.getItem().isFoil(stack));
            assertTrue(stack.getHoverName().getStyle().isBold());
            assertEquals(tier.color(), stack.getHoverName().getStyle().getColor().getValue());
        }
        assertEquals(SoundSource.MASTER, MysterySpawnerItem.OPEN_SOUND_SOURCE);
    }

    @Test void successfulTransactionConsumesOneAndStaleTransactionConsumesNothing() {
        var valid = MysterySpawners.create(MysterySpawnerTier.SIMPLE, 2);
        assertFalse(MysterySpawners.openAndConsume(valid, bound -> 0).isEmpty());
        assertEquals(1, valid.getCount());
        var stale = MysterySpawners.create(MysterySpawnerTier.SIMPLE, 2);
        stale.set(ModDataComponents.MYSTERY_SPAWNER.get(), new MysterySpawnerData(0, MysterySpawnerTier.SIMPLE));
        assertTrue(MysterySpawners.openAndConsume(stale, bound -> 0).isEmpty());
        assertEquals(2, stale.getCount());
    }

    private static Set<String> strings(java.util.List<Identifier> ids) {
        return ids.stream().map(Identifier::toString).collect(java.util.stream.Collectors.toSet());
    }
}
