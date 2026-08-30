package com.cosmicpve.reward.spawner;

import com.cosmicpve.data.component.MysterySpawnerData;
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public final class MysterySpawners {
    public static final List<Identifier> SIMPLE = ids(EntityType.SHEEP, EntityType.PIG, EntityType.ZOMBIE,
            EntityType.SPIDER, EntityType.SKELETON, EntityType.CHICKEN);
    public static final List<Identifier> ELITE = ids(EntityType.COW, EntityType.CREEPER, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.HUSK, EntityType.SNOW_GOLEM, EntityType.BLAZE, EntityType.SLIME, EntityType.ENDERMAN);
    public static final List<Identifier> MASTERY = ids(EntityType.IRON_GOLEM, EntityType.WITHER_SKELETON,
            EntityType.WITCH, EntityType.VINDICATOR, EntityType.GUARDIAN);

    private MysterySpawners() {}

    public static ItemStack create(MysterySpawnerTier tier, int count) {
        if (count < 1 || count > 64) throw new IllegalArgumentException("count must be in [1,64]");
        var item = switch (tier) {
            case SIMPLE -> ModItems.MYSTERY_SIMPLE_SPAWNER.get();
            case ELITE -> ModItems.MYSTERY_ELITE_SPAWNER.get();
            case MASTERY -> ModItems.MYSTERY_MASTERY_SPAWNER.get();
        };
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModDataComponents.MYSTERY_SPAWNER.get(),
                new MysterySpawnerData(MysterySpawnerData.CURRENT_DATA_VERSION, tier));
        return stack;
    }

    public static ItemStack open(MysterySpawnerData data, IntRandom random) {
        if (data == null || !data.isCurrent()) return ItemStack.EMPTY;
        List<Identifier> pool = pool(data.tier());
        return MobSpawners.create(pool.get(random.nextInt(pool.size())), 1);
    }

    /** Validates and resolves before consuming; invalid/stale stacks remain untouched. */
    public static ItemStack openAndConsume(ItemStack mystery, IntRandom random) {
        if (mystery.isEmpty()) return ItemStack.EMPTY;
        ItemStack reward = open(mystery.get(ModDataComponents.MYSTERY_SPAWNER.get()), random);
        if (!reward.isEmpty()) mystery.shrink(1);
        return reward;
    }

    public static List<Identifier> pool(MysterySpawnerTier tier) {
        return switch (tier) { case SIMPLE -> SIMPLE; case ELITE -> ELITE; case MASTERY -> MASTERY; };
    }

    private static List<Identifier> ids(EntityType<?>... types) {
        return java.util.Arrays.stream(types).map(BuiltInRegistries.ENTITY_TYPE::getKey).toList();
    }

    @FunctionalInterface public interface IntRandom { int nextInt(int bound); }
}
