package com.cosmicpve.entity.cinderwolf;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import java.util.Set;
import java.util.UUID;

/** Trial-owned wolf; the two registered types differ only in visual scale and encounter stats. */
public final class CinderWolfEntity extends Wolf {
    private Set<UUID> encounterParticipants = Set.of();
    private int meleeCooldown;
    private int meleeAttempts;
    public CinderWolfEntity(EntityType<? extends Wolf> type, Level level) { super(type, level); }

    public void setEncounterParticipants(Set<UUID> participants) {
        encounterParticipants = Set.copyOf(participants);
    }
    public int meleeAttempts() { return meleeAttempts; }

    @Override protected void registerGoals() {
        // Do not inherit tame-wolf sitting/panic/prey goals in a hostile Trial encounter.
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, ServerPlayer.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, ServerPlayer.class,
                1, true, false, (target, level) -> target instanceof ServerPlayer player
                        && encounterParticipants.contains(player.getUUID())));
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel) || isNoAi() || !isAlive()
                || encounterParticipants.isEmpty()) return;
        ServerPlayer nearest = null;
        double distance = Double.MAX_VALUE;
        for (UUID id : encounterParticipants) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(id);
            if (player == null || player.level() != serverLevel || !player.isAlive()) continue;
            double candidate = distanceToSqr(player);
            if (candidate < distance) { nearest = player; distance = candidate; }
        }
        setTarget(nearest);
        if (nearest == null) { getNavigation().stop(); return; }
        getLookControl().setLookAt(nearest, 30.0F, 30.0F);
        if (!isWithinMeleeAttackRange(nearest)) getNavigation().moveTo(nearest, 1.0);
        else getNavigation().stop();
        if (meleeCooldown > 0) --meleeCooldown;
        if (meleeCooldown == 0 && isWithinMeleeAttackRange(nearest)
                && getSensing().hasLineOfSight(nearest)) {
            swing(InteractionHand.MAIN_HAND);
            meleeAttempts++;
            doHurtTarget(serverLevel, nearest);
            meleeCooldown = 20;
        }
    }
}
