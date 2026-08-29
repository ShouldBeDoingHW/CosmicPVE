package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public final class RaidingRainbowService {
    public static final String ENCOUNTER_TAG = "cosmicpve_trial_raiding_rainbow";
    private static final String COLOR_PREFIX = "cosmicpve_rainbow_color_";
    public static final SpawnProfile ACTIVE_ZOMBIE = new SpawnProfile(1.0F, false, false, false, false);

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, InstanceBounds bounds, RandomSource random) {
        var sequence = RaidingRainbowLogic.shuffled(random);
        var platforms = new ArrayList<Integer>();
        for (int i = 0; i < 8; i++) platforms.add(i);
        for (int i = platforms.size() - 1; i > 0; i--) java.util.Collections.swap(platforms, i, random.nextInt(i + 1));
        platforms = new ArrayList<>(platforms.subList(0, RaidingRainbowLogic.ACTIVE_COLORS));
        platforms.sort(Integer::compareTo);
        return new TrialEncounterState(sequence.stream().map(RainbowColor::serialized).toList(), 0,
                platforms.stream().map(index -> "platform=" + index).toList(), List.of(), List.of());
    }

    public KillResult killed(ServerLevel level, TrialSession session, Zombie zombie, InstanceBounds bounds) {
        RainbowColor killed = color(zombie);
        var encounter = session.progress().encounter();
        if (killed == null || encounter.hiddenSequence().size() != RaidingRainbowLogic.ACTIVE_COLORS)
            return KillResult.ignored(encounter);
        var sequence = encounter.hiddenSequence().stream().map(RainbowColor::parse).toList();
        var step = RaidingRainbowLogic.evaluate(sequence, encounter.sequenceProgress(), killed);
        if (!step.correct()) {
            clearZombies(level, bounds);
            spawnFullSet(level, session, bounds);
            return new KillResult(false, false, killed, new TrialEncounterState(encounter.hiddenSequence(), 0,
                    encounter.pillarMaterials(), List.of(), List.of()));
        }
        return new KillResult(true, step.complete(), killed,
                new TrialEncounterState(encounter.hiddenSequence(), step.progress(), encounter.pillarMaterials(), List.of(), List.of()));
    }

    public void spawnFullSet(ServerLevel level, TrialSession session, InstanceBounds bounds) {
        List<BlockPos> spawns = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max()))
            if (level.getBlockState(pos).is(Blocks.GOLD_BLOCK)) spawns.add(pos.immutable());
        spawns.sort(BlockPos::compareTo);
        if (spawns.size() != 8) throw new IllegalStateException("Raiding Rainbow requires eight Gold spawn blocks, found " + spawns.size());
        var encounter = session.progress().encounter();
        var colors = encounter.hiddenSequence().stream().map(RainbowColor::parse).toList();
        var platforms = encounter.pillarMaterials().stream().map(value -> Integer.parseInt(value.substring("platform=".length()))).toList();
        if (colors.size() != RaidingRainbowLogic.ACTIVE_COLORS || platforms.size() != RaidingRainbowLogic.ACTIVE_COLORS)
            throw new IllegalStateException("Raiding Rainbow requires six persisted colors and platforms");
        for (int index = 0; index < RaidingRainbowLogic.ACTIVE_COLORS; index++) {
            RainbowColor color = colors.get(index);
            BlockPos pos = spawns.get(platforms.get(index));
            Zombie zombie = createZombie(level, session, color, pos);
            if (!level.addFreshEntity(zombie)) throw new IllegalStateException(
                    "Server rejected fresh Raiding Rainbow Zombie " + color.serialized() + " / " + zombie.getUUID());
        }
    }

    private static Zombie createZombie(ServerLevel level, TrialSession session, RainbowColor color, BlockPos pos) {
        Zombie zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.EVENT);
        if (zombie == null) throw new IllegalStateException("Could not create Raiding Rainbow Zombie");
        zombie.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        zombie.setHealth(ACTIVE_ZOMBIE.health()); zombie.setPersistenceRequired(); zombie.skipDropExperience();
        zombie.setNoAi(ACTIVE_ZOMBIE.noAi()); zombie.setSilent(ACTIVE_ZOMBIE.silent());
        zombie.setInvulnerable(ACTIVE_ZOMBIE.invulnerable()); zombie.setNoGravity(ACTIVE_ZOMBIE.noGravity());
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(color.wool()));
        zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
        zombie.addTag(ENCOUNTER_TAG); zombie.addTag(COLOR_PREFIX + color.serialized());
        zombie.addTag("cosmicpve_trial_session_" + session.sessionId());
        return zombie;
    }

    public void clearZombies(ServerLevel level, InstanceBounds bounds) {
        level.getEntitiesOfClass(Zombie.class, area(bounds),
                zombie -> zombie.getTags().contains(ENCOUNTER_TAG)).forEach(Zombie::discard);
    }
    public void activate(ServerLevel level, TrialSession session, InstanceBounds bounds) {
        clearZombies(level, bounds);
        spawnFullSet(level, session, bounds);
    }
    public static boolean encounterZombie(Zombie zombie) { return zombie.getTags().contains(ENCOUNTER_TAG); }
    public static RainbowColor color(Zombie zombie) {
        return zombie.getTags().stream().filter(tag -> tag.startsWith(COLOR_PREFIX)).findFirst()
                .map(tag -> RainbowColor.parse(tag.substring(COLOR_PREFIX.length()))).orElse(null);
    }
    private static net.minecraft.world.phys.AABB area(InstanceBounds bounds) {
        return new net.minecraft.world.phys.AABB(bounds.min().getX(), bounds.min().getY(), bounds.min().getZ(),
                bounds.max().getX() + 1.0, bounds.max().getY() + 1.0, bounds.max().getZ() + 1.0);
    }
    public record KillResult(boolean correct, boolean complete, RainbowColor color, TrialEncounterState nextState) {
        static KillResult ignored(TrialEncounterState state) { return new KillResult(false, false, null, state); }
    }
    public record SpawnProfile(float health, boolean noAi, boolean silent, boolean invulnerable, boolean noGravity) {}
}
