package com.cosmicpve.entity.undeadcorpse;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** Makes a summoned corpse defend its owner and assist the owner's attacks. */
final class OwnedAllyTargetGoal extends TargetGoal {
    private final UndeadCorpseEntity corpse;
    private final Function<LivingEntity, LivingEntity> target;
    private final ToIntFunction<LivingEntity> timestamp;
    private LivingEntity pending;
    private int lastTimestamp;

    OwnedAllyTargetGoal(UndeadCorpseEntity corpse, Function<LivingEntity, LivingEntity> target,
            ToIntFunction<LivingEntity> timestamp) {
        super(corpse, false); this.corpse = corpse; this.target = target; this.timestamp = timestamp;
    }
    @Override public boolean canUse() {
        var owner = corpse.getOwner();
        if (owner == null) return false;
        pending = target.apply(owner);
        int current = timestamp.applyAsInt(owner);
        return current != lastTimestamp && pending != null && corpse.canAttack(pending)
                && canAttack(pending, TargetingConditions.DEFAULT);
    }
    @Override public void start() {
        corpse.setTarget(pending);
        var owner = corpse.getOwner();
        if (owner != null) lastTimestamp = timestamp.applyAsInt(owner);
        super.start();
    }
}
