package com.cosmicpve.entity.woodlands;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/** Fixed mob-drop gate around one canonical Dense Woodlands reward-table selection. */
public final class DenseWoodlandsMobLoot {
    public static final net.minecraft.resources.Identifier BASIC_TABLE =
            CosmicPVE.id("adventure/dense_woodlands");

    private DenseWoodlandsMobLoot() {}

    public static boolean selected(RandomSource random, double chance) {
        return selected(random.nextDouble(), chance);
    }

    static boolean selected(double roll, double chance) { return roll < chance; }

    public static void drop(Entity entity, ServerLevel level, double chance) {
        if (!selected(entity.getRandom(), chance)) return;
        var context = new RewardGenerationContext(level.registryAccess(), entity.getRandom(), null);
        new RewardTableService(CosmicContent.repository(), new RewardGeneratorService())
                .roll(BASIC_TABLE, 1, context)
                .forEach(stack -> entity.spawnAtLocation(level, stack));
    }
}
