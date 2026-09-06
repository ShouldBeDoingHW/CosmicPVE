package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Shared-party runtime for the production Cave Diving pottery puzzle. */
public final class CaveDivingService {
    public static final BlockPos SPAWN_MARKER_LOCAL = new BlockPos(19, 30, 21);
    public static final BlockPos MODEL_MARKER_LOCAL = new BlockPos(13, 31, 9);
    public static final BlockPos SOLUTION_MARKER_LOCAL = new BlockPos(17, 31, 8);
    public static final BlockPos CRAFTING_TABLE_LOCAL = new BlockPos(13, 32, 22);
    public static final List<BlockPos> UNDERWATER_MARKERS_LOCAL = List.of(
            new BlockPos(9,2,1), new BlockPos(4,7,8), new BlockPos(20,13,9),
            new BlockPos(5,18,13), new BlockPos(4,22,26), new BlockPos(19,24,27));
    public static final List<Item> CANONICAL_SHERDS = List.of(
            Items.ANGLER_POTTERY_SHERD, Items.ARCHER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD,
            Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD, Items.BURN_POTTERY_SHERD,
            Items.DANGER_POTTERY_SHERD, Items.EXPLORER_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD);
    public static final int VALIDATION_INTERVAL_TICKS = 5;
    public static final SoundEvent REJECTION_SOUND = SoundEvents.ANVIL_DESTROY;
    public static final float REJECTION_PITCH = 0.8F;

    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public Initialization initialize(ServerLevel level, TrialSession session, BlockPos origin,
            InstanceBounds bounds, RandomSource random) {
        PotAnswer model = randomAnswer(random);
        BlockPos modelPosition = origin.offset(MODEL_MARKER_LOCAL).above();
        BlockPos solutionPosition = origin.offset(SOLUTION_MARKER_LOCAL).above();
        placePot(level, modelPosition, model);

        List<Item> underwaterSherds = generateUnderwaterSherds(model.decorations(), random);
        Map<BlockPos, PotAnswer> underwater = new java.util.LinkedHashMap<>();
        for (int i = 0; i < UNDERWATER_MARKERS_LOCAL.size(); i++) {
            BlockPos position = origin.offset(UNDERWATER_MARKERS_LOCAL.get(i));
            if (!level.getBlockState(position).is(Blocks.BRICKS))
                throw new IllegalStateException("Cave Diving Brick marker missing at " + position);
            PotAnswer answer = new PotAnswer(List.copyOf(underwaterSherds.subList(i * 4, i * 4 + 4)),
                    randomHorizontal(random));
            placePot(level, position, answer);
            underwater.put(position, answer);
        }
        Attempt attempt = new Attempt(session.sessionId(), bounds, modelPosition, solutionPosition,
                model, Map.copyOf(underwater), new LinkedHashSet<>(), false);
        attempts.put(session.sessionId(), attempt);
        return new Initialization(encounter(attempt), attempt);
    }

    public ValidationResult tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        return validate(attempt, attempt == null ? null : readPot(level, attempt.solutionPosition()));
    }

    public boolean allowsBreak(TrialSession session, BlockPos pos) {
        Attempt attempt = attempts.get(session.sessionId());
        return breakable(attempt, pos);
    }

    public boolean allowsSolutionPlacement(TrialSession session, BlockPos pos, ItemStack stack) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && attempt.solutionPosition().equals(pos) && stack.is(Items.DECORATED_POT);
    }

    public boolean rejectsPotPlacement(TrialSession session, BlockPos pos, ItemStack stack) {
        Attempt attempt = attempts.get(session.sessionId());
        return rejectsPotPlacement(attempt, pos, stack);
    }

    static boolean rejectsPotPlacement(Attempt attempt, BlockPos pos, ItemStack stack) {
        return attempt != null && attempt.bounds().contains(pos) && stack.is(Items.DECORATED_POT)
                && !attempt.solutionPosition().equals(pos);
    }

    /** One-shot sweep after structure updates and before ROOM_ACTIVE; later puzzle drops are never vacuumed. */
    public int cleanupStartupItems(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        return cleanupStartupItems(level, attempt);
    }

    static int cleanupStartupItems(ServerLevel level, Attempt attempt) {
        if (attempt == null || attempt.startupItemsCleaned()) return 0;
        attempt.startupItemsCleaned(true);
        var min = attempt.bounds().min();
        var max = attempt.bounds().max();
        var area = new net.minecraft.world.phys.AABB(min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
        var items = level.getEntitiesOfClass(ItemEntity.class, area);
        items.forEach(ItemEntity::discard);
        return items.size();
    }

    public boolean allowsCraftingTableUse(TrialSession session, ServerLevel level, BlockPos pos) {
        Attempt attempt = attempts.get(session.sessionId());
        return attempt != null && pos.equals(attempt.bounds().min().offset(CRAFTING_TABLE_LOCAL))
                && level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    /** Replaces vanilla pot loot with the four authored side sherds exactly once. */
    public boolean replaceUnderwaterDrops(TrialSession session, BlockDropsEvent event) {
        Attempt attempt = attempts.get(session.sessionId());
        List<Item> claimed = claimSherds(attempt, event.getPos());
        if (attempt != null && attempt.solutionPosition().equals(event.getPos())
                && event.getBlockEntity() instanceof DecoratedPotBlockEntity pot)
            claimed = pot.getDecorations().ordered();
        if (claimed.isEmpty()) return false;
        event.getDrops().clear();
        for (Item sherd : claimed) {
            BlockPos pos = event.getPos();
            event.getDrops().add(new ItemEntity(event.getLevel(), pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, new ItemStack(sherd)));
        }
        return true;
    }

    static boolean breakable(Attempt attempt, BlockPos pos) {
        return attempt != null && (attempt.solutionPosition().equals(pos)
                || attempt.underwater().containsKey(pos) && !attempt.harvested().contains(pos));
    }

    static List<Item> claimSherds(Attempt attempt, BlockPos pos) {
        if (attempt == null || !attempt.underwater().containsKey(pos) || !attempt.harvested().add(pos)) return List.of();
        return attempt.underwater().get(pos).decorations();
    }

    public String status(UUID sessionId, ServerLevel level) {
        Attempt attempt = attempts.get(sessionId);
        if (attempt == null) return "cave diving state unavailable";
        PotAnswer submitted = readPot(level, attempt.solutionPosition());
        return "model=" + describe(attempt.model()) + " modelPos=" + attempt.modelPosition()
                + " solutionPos=" + attempt.solutionPosition() + " submitted=" + describe(submitted)
                + " sources=" + attempt.underwater().entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + describe(entry.getValue()))
                        .collect(java.util.stream.Collectors.joining(",", "[", "]"))
                + " harvested=" + attempt.harvested().size() + "/6 matches=" + matches(attempt.model(), submitted);
    }

    public void cleanup(UUID sessionId) { attempts.remove(sessionId); }

    public static PotAnswer randomAnswer(RandomSource random) {
        var decorations = new ArrayList<Item>(4);
        for (int i = 0; i < 4; i++) decorations.add(CANONICAL_SHERDS.get(random.nextInt(CANONICAL_SHERDS.size())));
        return new PotAnswer(decorations, randomHorizontal(random));
    }

    public static List<Item> generateUnderwaterSherds(List<Item> model, RandomSource random) {
        if (model.size() != 4 || model.stream().anyMatch(item -> !CANONICAL_SHERDS.contains(item)))
            throw new IllegalArgumentException("Cave Diving model requires four canonical sherds");
        var result = new ArrayList<Item>(24);
        for (Item sherd : model) { result.add(sherd); result.add(sherd); }
        while (result.size() < 24) result.add(CANONICAL_SHERDS.get(random.nextInt(CANONICAL_SHERDS.size())));
        shuffle(result, random);
        return List.copyOf(result);
    }

    public static boolean matches(PotAnswer model, PotAnswer submitted) {
        return model != null && submitted != null && worldSides(model).equals(worldSides(submitted));
    }

    /** Renderer decorations are back/left/right/front relative to the block's facing. */
    public static List<Item> worldSides(PotAnswer answer) {
        var sides = new java.util.EnumMap<Direction, Item>(Direction.class);
        sides.put(answer.facing(), answer.decorations().get(3));
        sides.put(answer.facing().getOpposite(), answer.decorations().get(0));
        sides.put(answer.facing().getClockWise(), answer.decorations().get(1));
        sides.put(answer.facing().getCounterClockWise(), answer.decorations().get(2));
        return List.of(sides.get(Direction.NORTH), sides.get(Direction.EAST),
                sides.get(Direction.SOUTH), sides.get(Direction.WEST));
    }

    static ValidationResult validate(Attempt attempt, PotAnswer submitted) {
        if (attempt == null || attempt.solved()) return ValidationResult.NONE;
        if (submitted == null) {
            attempt.lastRejected(null);
            return ValidationResult.NONE;
        }
        if (matches(attempt.model(), submitted)) {
            attempt.solved(true);
            return ValidationResult.SOLVED;
        }
        if (submitted.equals(attempt.lastRejected())) return ValidationResult.NONE;
        attempt.lastRejected(submitted);
        return ValidationResult.REJECTED;
    }

    private static void placePot(ServerLevel level, BlockPos pos, PotAnswer answer) {
        var state = Blocks.DECORATED_POT.defaultBlockState().setValue(DecoratedPotBlock.HORIZONTAL_FACING, answer.facing());
        level.setBlock(pos, state, 3);
        if (!(level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot))
            throw new IllegalStateException("Decorated Pot block entity missing at " + pos);
        pot.applyComponentsFromItemStack(DecoratedPotBlockEntity.createDecoratedPotItem(answer.asDecorations()));
        pot.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private static PotAnswer readPot(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot)) return null;
        Direction facing = level.getBlockState(pos).getOptionalValue(DecoratedPotBlock.HORIZONTAL_FACING).orElse(Direction.NORTH);
        return new PotAnswer(pot.getDecorations().ordered(), facing);
    }

    private static Direction randomHorizontal(RandomSource random) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(random);
    }

    private static <T> void shuffle(List<T> values, RandomSource random) {
        for (int i = values.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1); T value = values.get(i); values.set(i, values.get(j)); values.set(j, value);
        }
    }

    private static TrialEncounterState encounter(Attempt attempt) {
        List<String> model = new ArrayList<>();
        model.add("facing=" + attempt.model().facing().getSerializedName());
        attempt.model().decorations().forEach(item -> model.add(item.toString()));
        return new TrialEncounterState(model, 0, List.of(), List.copyOf(attempt.underwater().keySet()),
                List.of(), List.of(attempt.modelPosition(), attempt.solutionPosition()), List.of());
    }

    private static String describe(PotAnswer answer) {
        return answer == null ? "none" : answer.facing().getSerializedName() + ":" + answer.decorations();
    }

    public record PotAnswer(List<Item> decorations, Direction facing) {
        public PotAnswer {
            decorations = List.copyOf(decorations);
            if (decorations.size() != 4 || !facing.getAxis().isHorizontal())
                throw new IllegalArgumentException("Pot answer requires four sides and a horizontal facing");
        }
        PotDecorations asDecorations() {
            return new PotDecorations(decorations.get(0), decorations.get(1), decorations.get(2), decorations.get(3));
        }
    }
    public record Initialization(TrialEncounterState encounter, Attempt attempt) {}
    public enum ValidationResult { NONE, REJECTED, SOLVED }
    public static final class Attempt {
        private final UUID sessionId; private final InstanceBounds bounds; private final BlockPos modelPosition;
        private final BlockPos solutionPosition; private final PotAnswer model; private final Map<BlockPos, PotAnswer> underwater;
        private final Set<BlockPos> harvested; private boolean solved; private PotAnswer lastRejected;
        private boolean startupItemsCleaned;
        Attempt(UUID sessionId, InstanceBounds bounds, BlockPos modelPosition, BlockPos solutionPosition, PotAnswer model,
                Map<BlockPos, PotAnswer> underwater, Set<BlockPos> harvested, boolean solved) {
            this.sessionId=sessionId; this.bounds=bounds; this.modelPosition=modelPosition; this.solutionPosition=solutionPosition;
            this.model=model; this.underwater=underwater; this.harvested=harvested; this.solved=solved;
        }
        public UUID sessionId(){return sessionId;} public InstanceBounds bounds(){return bounds;}
        public BlockPos modelPosition(){return modelPosition;} public BlockPos solutionPosition(){return solutionPosition;}
        public PotAnswer model(){return model;} public Map<BlockPos,PotAnswer> underwater(){return underwater;}
        public Set<BlockPos> harvested(){return harvested;} public boolean solved(){return solved;} void solved(boolean value){solved=value;}
        PotAnswer lastRejected(){return lastRejected;} void lastRejected(PotAnswer value){lastRejected=value;}
        boolean startupItemsCleaned(){return startupItemsCleaned;} void startupItemsCleaned(boolean value){startupItemsCleaned=value;}
    }
}
