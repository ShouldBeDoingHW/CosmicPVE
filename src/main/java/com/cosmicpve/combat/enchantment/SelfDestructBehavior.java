package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;

/** Low-health defensive TNT burst with ownership scoped to each spawned entity. */
public final class SelfDestructBehavior {
    public static final double HEALTH_THRESHOLD = 0.15;
    public static final long COOLDOWN_TICKS = 1_200L;
    public static final int TNT_FUSE_TICKS = 15;
    public static final String TNT_TAG = CosmicPVE.MOD_ID + ".self_destruct_tnt";
    public static final List<CardinalOffset> OFFSETS = List.of(
            new CardinalOffset(0, -1), new CardinalOffset(0, 1),
            new CardinalOffset(1, 0), new CardinalOffset(-1, 0));

    private SelfDestructBehavior() {}

    public static boolean belowThreshold(float health, float maximumHealth) {
        return health > 0.0F && maximumHealth > 0.0F && health / maximumHealth < HEALTH_THRESHOLD;
    }

    public static int activate(LivingEntity wearer) {
        if (!(wearer.level() instanceof ServerLevel level) || wearer.isDeadOrDying()) return 0;
        int spawned = 0;
        for (var offset : OFFSETS) {
            var tnt = new PrimedTnt(level, wearer.getX() + offset.x(), wearer.getY(),
                    wearer.getZ() + offset.z(), wearer);
            tnt.setFuse(TNT_FUSE_TICKS);
            tnt.addTag(TNT_TAG);
            if (level.addFreshEntity(tnt)) spawned++;
        }
        return spawned;
    }

    public static boolean isEventTnt(Entity entity) {
        return entity instanceof PrimedTnt && entity.getTags().contains(TNT_TAG);
    }

    public static boolean isImmuneOwner(LivingEntity target, Entity directSource) {
        return directSource instanceof PrimedTnt tnt && isEventTnt(tnt) && tnt.getOwner() == target;
    }

    public record CardinalOffset(int x, int z) {}
}
