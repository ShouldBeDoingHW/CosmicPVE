package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.entity.cinderwolf.CinderWolfEntity;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.registry.ModBlocks;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.trial.TrialSession;
import com.cosmicpve.trial.TrialTitleService;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.AABB;

/** Per-session encounter state. The authored structure supplies all three positional markers. */
public final class CinderWolfService {
    public static final BlockPos PLAYER_MARKER_LOCAL = new BlockPos(14, 0, 1);
    public static final BlockPos BOSS_MARKER_LOCAL = new BlockPos(27, 0, 25);
    public static final BlockPos EXIT_MARKER_LOCAL = new BlockPos(1, 6, 32);
    public static final int FIREBALL_INTERVAL = 160;
    public static final int PUP_INTERVAL = 240;
    public static final int FIRE_TICKS = 100;
    public static final double EXPLOSION_RADIUS = 2.5;
    public static final double FIREBALL_TRUE_DAMAGE = 2.0;
    public static final String PROJECTILE_TAG = CosmicPVE.MOD_ID + ".cinder_wolf_fireball";
    public static final List<Direction> FIREBALL_DIRECTIONS = List.of(
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public static double bossHealth(int participants) {
        if (participants < 1 || participants > 4) throw new IllegalArgumentException("Cinder Wolf party size must be 1-4");
        return 200.0 + 150.0 * (participants - 1);
    }
    public static int pupsPerWave(int participants) {
        if (participants < 1 || participants > 4) throw new IllegalArgumentException("Cinder Wolf party size must be 1-4");
        return 2 + participants;
    }
    public static TrueDamagePacket fireballPacket() {
        return new TrueDamagePacket(CosmicPVE.id("trial/cinder_wolf_fireball"), FIREBALL_TRUE_DAMAGE,
                true, true, true);
    }

    public void initialize(ServerLevel level, TrialSession session, BlockPos origin, InstanceBounds bounds) {
        fireproofMangrove(level, bounds);
        BlockPos bossPos = origin.offset(BOSS_MARKER_LOCAL);
        BlockPos exitPos = origin.offset(EXIT_MARKER_LOCAL);
        if (!bounds.contains(bossPos) || !level.getBlockState(bossPos).is(Blocks.STRIPPED_CHERRY_WOOD)
                || !bounds.contains(exitPos) || !level.getBlockState(exitPos).is(Blocks.DIAMOND_BLOCK))
            throw new IllegalStateException("Cinder Wolf structure markers are missing");
        CinderWolfEntity boss = ModEntities.CINDER_WOLF.get().create(level, EntitySpawnReason.EVENT);
        if (boss == null) throw new IllegalStateException("Could not create Cinder Wolf boss");
        boss.setEncounterParticipants(Set.copyOf(session.participants()));
        boss.setPos(bossPos.getX() + .5, bossPos.getY() + 1.0, bossPos.getZ() + .5);
        setStats(boss, bossHealth(session.participants().size()), 2.5, 1.0, .35, 7.5);
        boss.setNoAi(true);
        if (!level.addFreshEntity(boss)) throw new IllegalStateException("Could not spawn Cinder Wolf boss");
        level.setBlock(bossPos, Blocks.NETHER_BRICKS.defaultBlockState(), 3);
        attempts.put(session.sessionId(), new Attempt(bounds, boss.getUUID(), exitPos, session.participants().size()));
    }

    public void activate(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return;
        if (level.getEntity(attempt.bossId) instanceof CinderWolfEntity boss) boss.setNoAi(false);
        attempt.active = true;
        attempt.ticks = 0;
    }

    public boolean tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || !attempt.active) return false;
        if (attempt.portalCreated) return enteredPortal(level, session, attempt);
        Entity entity = level.getEntity(attempt.bossId);
        if (!(entity instanceof CinderWolfEntity boss) || !boss.isAlive()) {
            createPortal(level, session, attempt);
            return enteredPortal(level, session, attempt);
        }
        targetParticipants(level, session, boss);
        for (UUID pupId : attempt.pups) {
            if (level.getEntity(pupId) instanceof CinderWolfEntity pup && pup.isAlive())
                targetParticipants(level, session, pup);
        }
        attempt.ticks++;
        if (attempt.ticks % FIREBALL_INTERVAL == 0) fireVolley(level, boss, attempt);
        if (attempt.ticks % PUP_INTERVAL == 0) pupWave(level, boss, attempt, session);
        return false;
    }

    public boolean bossDeath(ServerLevel level, TrialSession session, CinderWolfEntity wolf) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || !attempt.bossId.equals(wolf.getUUID()) || attempt.portalCreated) return false;
        createPortal(level, session, attempt);
        return true;
    }

    public boolean physicallyCompleted(UUID sessionId) {
        Attempt attempt = attempts.get(sessionId);
        return attempt != null && attempt.portalCreated && attempt.exitEntered;
    }

    public boolean projectileImpact(ServerLevel level, SmallFireball projectile, TrialSession session) {
        if (!projectile.getTags().contains(PROJECTILE_TAG)) return false;
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || !attempt.projectiles.remove(projectile.getUUID())) {
            projectile.discard(); return true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, projectile.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
        Entity boss = level.getEntity(attempt.bossId);
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player == null || player.level() != level || !player.isAlive()
                    || player.distanceToSqr(projectile) > EXPLOSION_RADIUS * EXPLOSION_RADIUS) continue;
            CosmicCombat.childActions().deliverTrueRoot(player,
                    boss instanceof CinderWolfEntity wolf ? wolf : null,
                    fireballPacket(), RecursionPolicy.NO_PROCS);
            player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), FIRE_TICKS));
        }
        projectile.discard();
        return true;
    }

    public void cleanup(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.remove(sessionId);
        if (attempt == null) return;
        Entity boss = level.getEntity(attempt.bossId);
        if (boss != null) boss.discard();
        attempt.pups.forEach(id -> { Entity pup = level.getEntity(id); if (pup != null) pup.discard(); });
        attempt.projectiles.forEach(id -> { Entity projectile = level.getEntity(id); if (projectile != null) projectile.discard(); });
        if (attempt.portalCreated) {
            level.setBlock(attempt.exitMarker.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(attempt.exitMarker.above(2), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void setStats(CinderWolfEntity wolf, double health, double armor,
                                 double toughness, double speed, double damage) {
        wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        wolf.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        wolf.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(toughness);
        wolf.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        wolf.setHealth((float) health);
    }

    private static void targetParticipants(ServerLevel level, TrialSession session, CinderWolfEntity wolf) {
        ServerPlayer nearest = null;
        double distance = Double.MAX_VALUE;
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player == null || player.level() != level || !player.isAlive()) continue;
            double candidate = wolf.distanceToSqr(player);
            if (candidate < distance) { nearest = player; distance = candidate; }
        }
        wolf.setTarget(nearest);
    }

    private static void fireproofMangrove(ServerLevel level, InstanceBounds bounds) {
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            var state = level.getBlockState(pos);
            if (state.is(Blocks.STRIPPED_MANGROVE_WOOD)) {
                level.setBlock(pos, ModBlocks.CINDERPROOF_MANGROVE_WOOD.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)), 2);
            }
        }
    }

    private static void fireVolley(ServerLevel level, CinderWolfEntity boss, Attempt attempt) {
        for (Direction direction : FIREBALL_DIRECTIONS) {
            SmallFireball ball = new SmallFireball(level, boss,
                    new Vec3(direction.getStepX(), 0, direction.getStepZ()));
            ball.setPos(boss.getX() + direction.getStepX() * 1.5, boss.getY() + .6,
                    boss.getZ() + direction.getStepZ() * 1.5);
            ball.addTag(PROJECTILE_TAG);
            if (level.addFreshEntity(ball)) attempt.projectiles.add(ball.getUUID());
        }
    }

    private static void pupWave(ServerLevel level, CinderWolfEntity boss, Attempt attempt, TrialSession session) {
        for (int index = 0; index < pupsPerWave(attempt.participants); index++) {
            CinderWolfEntity pup = ModEntities.CINDER_PUP.get().create(level, EntitySpawnReason.EVENT);
            if (pup == null) continue;
            pup.setEncounterParticipants(Set.copyOf(session.participants()));
            if (!placePupSafely(level, boss, pup, attempt.bounds, index, pupsPerWave(attempt.participants))) continue;
            setStats(pup, 5.0, 0.0, 0.0, .28, 6.0);
            if (level.addFreshEntity(pup)) {
                targetParticipants(level, session, pup);
                attempt.pups.add(pup.getUUID());
            }
        }
    }

    private static boolean placePupSafely(ServerLevel level, CinderWolfEntity boss, CinderWolfEntity pup,
                                          InstanceBounds bounds, int index, int count) {
        int centerX = net.minecraft.util.Mth.floor(boss.getX());
        int centerZ = net.minecraft.util.Mth.floor(boss.getZ());
        int feetY = net.minecraft.util.Mth.floor(boss.getY());
        for (int radius = 2; radius <= 6; radius++) {
            for (int offset = 0; offset < 32; offset++) {
                double angle = 2.0 * Math.PI * (index / (double) count + offset / 32.0);
                BlockPos feet = new BlockPos(centerX + (int) Math.round(radius * Math.cos(angle)),
                        feetY, centerZ + (int) Math.round(radius * Math.sin(angle)));
                if (!bounds.contains(feet) || !bounds.contains(feet.above())
                        || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        || !level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) continue;
                pup.setPos(feet.getX() + .5, feet.getY(), feet.getZ() + .5);
                if (level.noCollision(pup)) return true;
            }
        }
        return false;
    }

    private static void createPortal(ServerLevel level, TrialSession session, Attempt attempt) {
        if (attempt.portalCreated) return;
        level.setBlock(attempt.exitMarker.above(), ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
        level.setBlock(attempt.exitMarker.above(2), ModBlocks.TRIAL_GATEWAY.get().defaultBlockState(), 3);
        attempt.portalCreated = true;
        for (UUID participant : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null && player.level() == level) TrialTitleService.cinderWolfDown(player);
        }
    }

    private static boolean participantInPortal(ServerLevel level, TrialSession session, BlockPos marker) {
        AABB portal = new AABB(marker.above()).expandTowards(0, 2, 0);
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.level() == level && player.getBoundingBox().intersects(portal)) return true;
        }
        return false;
    }

    private static boolean enteredPortal(ServerLevel level, TrialSession session, Attempt attempt) {
        if (!participantInPortal(level, session, attempt.exitMarker)) return false;
        attempt.exitEntered = true;
        return true;
    }

    private static final class Attempt {
        final InstanceBounds bounds;
        final UUID bossId;
        final BlockPos exitMarker;
        final int participants;
        final Set<UUID> pups = new LinkedHashSet<>();
        final Set<UUID> projectiles = new LinkedHashSet<>();
        boolean active;
        boolean portalCreated;
        boolean exitEntered;
        int ticks;
        Attempt(InstanceBounds bounds, UUID bossId, BlockPos exitMarker, int participants) {
            this.bounds = bounds; this.bossId = bossId; this.exitMarker = exitMarker; this.participants = participants;
        }
    }
}
