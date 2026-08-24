package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Blocks;

/** Event-driven runtime state for the destructive Bomb Squad Trial room. */
public final class BombSquadService {
    public static final String ENCOUNTER_TAG = "cosmicpve.bomb_squad_creeper";
    public static final List<BlockPos> SPAWN_MARKERS_LOCAL = List.of(
            new BlockPos(2, 1, 2), new BlockPos(2, 1, 42),
            new BlockPos(42, 1, 2), new BlockPos(42, 1, 42));
    public static final List<BlockPos> SUPPLY_PLATES_LOCAL = List.of(
            new BlockPos(4, 2, 4), new BlockPos(4, 2, 40),
            new BlockPos(40, 2, 4), new BlockPos(40, 2, 40));
    public static final List<BlockPos> EXIT_MARKERS_LOCAL = List.of(
            new BlockPos(2,1,21), new BlockPos(2,1,23), new BlockPos(14,1,21), new BlockPos(14,1,23),
            new BlockPos(16,1,30), new BlockPos(21,1,2), new BlockPos(21,1,9), new BlockPos(21,1,16),
            new BlockPos(21,1,42), new BlockPos(23,1,23), new BlockPos(23,1,35), new BlockPos(23,1,37),
            new BlockPos(28,1,7), new BlockPos(28,1,9), new BlockPos(28,1,21), new BlockPos(30,1,23),
            new BlockPos(35,1,21), new BlockPos(37,1,21), new BlockPos(37,1,28));

    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public Initialization initialize(ServerLevel level, TrialSession session, BlockPos origin,
            InstanceBounds bounds, RandomSource random) {
        Layout layout = selectLayout(random);
        for (BlockPos local : SPAWN_MARKERS_LOCAL) {
            BlockPos marker = origin.offset(local);
            if (level.getBlockState(marker).is(Blocks.EMERALD_BLOCK)) level.setBlock(marker,
                    InstanceStructureService.inferredFloorReplacement(level, marker, "Bomb Squad spawn"), 3);
        }
        var exits = new ArrayList<Exit>();
        for (BlockPos local : EXIT_MARKERS_LOCAL) {
            BlockPos marker = origin.offset(local);
            if (!level.getBlockState(marker).is(Blocks.DIAMOND_BLOCK)) {
                throw new IllegalStateException("Bomb Squad exit marker missing at " + marker);
            }
            var floor = InstanceStructureService.inferredFloorReplacement(level, marker, "Bomb Squad exit");
            level.setBlock(marker, floor, 3);
            BlockPos plate = marker.above();
            GridCell cell = gridCell(local);
            if (layout.activeExitLocals().contains(local)) level.setBlock(plate,
                    Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 3);
            exits.add(new Exit(marker, plate, cell, layout.activeExitLocals().contains(local)));
        }
        List<BlockPos> supplies = SUPPLY_PLATES_LOCAL.stream().map(origin::offset).toList();
        BlockPos spawnMarker = origin.offset(layout.spawnLocal());
        Attempt attempt = new Attempt(session.sessionId(), bounds, spawnMarker, layout.spawnCell(),
                supplies, List.copyOf(exits), new LinkedHashSet<>());
        attempts.put(session.sessionId(), attempt);
        List<BlockPos> activePlates = exits.stream().filter(Exit::active).map(Exit::plate).toList();
        TrialEncounterState encounter = new TrialEncounterState(List.of(), 0, List.of(), activePlates,
                List.of(), List.of(spawnMarker), List.of());
        return new Initialization(spawnMarker.above(), encounter, attempt);
    }

    public Layout selectLayout(RandomSource random) {
        BlockPos spawn = SPAWN_MARKERS_LOCAL.get(random.nextInt(SPAWN_MARKERS_LOCAL.size()));
        GridCell start = gridCell(spawn);
        var eligible = new ArrayList<BlockPos>();
        for (BlockPos marker : EXIT_MARKERS_LOCAL) {
            GridCell cell = gridCell(marker);
            if (Math.max(Math.abs(cell.column() - start.column()), Math.abs(cell.row() - start.row())) > 1) eligible.add(marker);
        }
        if (eligible.size() < 2) throw new IllegalStateException("Bomb Squad start " + start + " has fewer than two exits");
        BlockPos first = eligible.remove(random.nextInt(eligible.size()));
        BlockPos second = eligible.remove(random.nextInt(eligible.size()));
        return new Layout(spawn, start, List.of(first, second));
    }

    /** The structure's six chambers occupy stable seven-block grid bands 1..42 on each horizontal axis. */
    public static GridCell gridCell(BlockPos local) {
        int column = Math.max(0, Math.min(5, (local.getX() - 1) / 7));
        int row = Math.max(0, Math.min(5, (local.getZ() - 1) / 7));
        return new GridCell(column, row);
    }

    public boolean dispenseEgg(ServerPlayer player, TrialSession session, BlockPos plate) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || !attempt.supplies().contains(plate) || hasIssuedEgg(player)) return false;
        return player.getInventory().add(issuedEgg());
    }

    public static ItemStack issuedEgg() {
        ItemStack egg = new ItemStack(Items.CREEPER_SPAWN_EGG);
        CompoundTag entityTag = new CompoundTag();
        ListTag tags = new ListTag(); tags.add(StringTag.valueOf(ENCOUNTER_TAG)); entityTag.put("Tags", tags);
        egg.set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.CREEPER, entityTag));
        egg.set(DataComponents.CUSTOM_NAME, Component.literal("Bomb Squad Creeper Spawn Egg"));
        return egg;
    }

    public static boolean issuedEgg(ItemStack stack) {
        if (!stack.is(Items.CREEPER_SPAWN_EGG)) return false;
        TypedEntityData<?> data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null || data.type() != EntityType.CREEPER) return false;
        return data.copyTagWithoutId().getListOrEmpty("Tags").stream()
                .anyMatch(tag -> tag.asString().filter(ENCOUNTER_TAG::equals).isPresent());
    }

    public static boolean hasIssuedEgg(ServerPlayer player) {
        return player.getInventory().contains(BombSquadService::issuedEgg);
    }

    public boolean acceptEncounterCreeper(ServerLevel level, TrialSession session, Creeper creeper) {
        Attempt attempt = attempts.get(session.sessionId());
        if (!encounterCreeper(creeper) || attempt == null || !attempt.bounds().contains(creeper.blockPosition())) return false;
        attempt.creepers().add(creeper.getUUID());
        return true;
    }

    public static boolean encounterCreeper(Entity entity) {
        return entity instanceof Creeper && entity.getTags().contains(ENCOUNTER_TAG);
    }

    public boolean trackedEncounterCreeper(TrialSession session, Entity entity) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && encounterCreeper(entity) && attempt.creepers().contains(entity.getUUID());
    }

    public boolean activeExit(TrialSession session, BlockPos plate) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && attempt.exits().stream().anyMatch(exit -> exit.active() && exit.plate().equals(plate));
    }

    public boolean specialPlate(TrialSession session, BlockPos pos) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && (attempt.supplies().contains(pos)
                || attempt.exits().stream().anyMatch(exit -> exit.active() && exit.plate().equals(pos)));
    }

    public boolean allowsExplosionBlock(ServerLevel level, TrialSession session, Entity source, BlockPos pos) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && trackedEncounterCreeper(session, source) && attempt.bounds().contains(pos)
                && !specialPlate(session, pos) && level.getBlockState(pos).is(Blocks.STONE);
    }

    public static boolean allowsExplosionMaterial(boolean validEncounter, boolean inBounds,
            boolean specialPlate, net.minecraft.world.level.block.state.BlockState state) {
        return validEncounter && inBounds && !specialPlate && state.is(Blocks.STONE);
    }

    public String status(UUID sessionId) {
        Attempt attempt = attempts.get(sessionId);
        if (attempt == null) return "bomb squad state unavailable";
        return "spawn=" + attempt.spawnMarker() + " cell=" + attempt.spawnCell()
                + " supplies=" + attempt.supplies() + " exits="
                + attempt.exits().stream().filter(Exit::active).map(exit -> exit.plate() + "@" + exit.cell()).toList()
                + " creepers=" + attempt.creepers().size();
    }

    public int trackedCreepers(UUID sessionId) {
        Attempt attempt = attempts.get(sessionId); return attempt == null ? 0 : attempt.creepers().size();
    }

    public void cleanup(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.remove(sessionId);
        if (attempt == null) return;
        for (UUID id : attempt.creepers()) {
            Entity entity = level.getEntity(id); if (entity != null) entity.discard();
        }
    }

    public record GridCell(int column, int row) {}
    public record Layout(BlockPos spawnLocal, GridCell spawnCell, List<BlockPos> activeExitLocals) {
        public Layout { activeExitLocals = List.copyOf(activeExitLocals); }
    }
    public record Exit(BlockPos marker, BlockPos plate, GridCell cell, boolean active) {}
    public record Attempt(UUID sessionId, InstanceBounds bounds, BlockPos spawnMarker, GridCell spawnCell,
            List<BlockPos> supplies, List<Exit> exits, Set<UUID> creepers) {}
    public record Initialization(BlockPos participantSpawn, TrialEncounterState encounter, Attempt attempt) {}
}
