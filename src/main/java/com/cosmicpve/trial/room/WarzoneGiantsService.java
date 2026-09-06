package com.cosmicpve.trial.room;

import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.trial.TrialEncounterState;
import com.cosmicpve.trial.TrialSession;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Room-local, pre-indexed Warzone Giants encounter state. */
public final class WarzoneGiantsService {
    public static final String ENCOUNTER_TAG = "cosmicpve_trial_warzone_giant";
    private static final String TIER_PREFIX = "cosmicpve_warzone_tier_";
    public static final int NORMAL_TICKS = 200;
    public static final int WARNING_TICKS = 100;
    public static final int ABSENT_TICKS = 60;
    public static final double GIANT_SCALE = 6.0D;
    public static final double GIANT_HEALTH = 100.0D;
    public static final double GIANT_SPEED_INCREASE = 0.05D;
    public static final double GIANT_ATTACK_DAMAGE = 1.0D;
    public static final int STRENGTH_TICKS = 36_000;
    public static final int STRENGTH_AMPLIFIER = 1;
    public static final int COLLISION_RECOVERY_CADENCE = 10;
    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public TrialEncounterState initialize(ServerLevel level, TrialSession session, InstanceBounds bounds, RandomSource random) {
        var floor = indexFloor(level, bounds);
        for (FloorColor color : FloorColor.values())
            if (floor.get(color).isEmpty()) throw new IllegalStateException("Warzone floor is missing " + color.serialized);
        List<GiantTier> tiers = selectTiers(session.participants().size(), random);
        var attempt = new Attempt(session.sessionId(), bounds, floor, new LinkedHashMap<>(), tiers,
                HazardPhase.NORMAL, NORMAL_TICKS, List.of(), random,
                floor.values().stream().flatMap(map -> map.keySet().stream()).mapToInt(BlockPos::getY).min().orElseThrow());
        attempts.put(session.sessionId(), attempt);
        for (GiantTier tier : tiers) spawnGiant(level, session, attempt, tier);
        return encounter(attempt);
    }

    public TickResult tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return TickResult.EMPTY;
        var fallen = new ArrayList<UUID>();
        int playerFailY = attempt.floorY - 6;
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.getY() <= playerFailY) fallen.add(id);
        }
        recoverInvalidGiants(level, session, attempt);

        int countdown = 0;
        boolean hazardSound = false;
        List<FloorColor> warning = List.of();
        if (attempt.phase == HazardPhase.ABSENT && attempt.soundElapsed >= 0) {
            attempt.soundElapsed++;
            hazardSound = hazardSoundTick(attempt.soundElapsed);
        }
        if (attempt.phase == HazardPhase.WARNING && attempt.remaining % 20 == 0) {
            countdown = attempt.remaining / 20;
            warning = attempt.selected;
        }
        attempt.remaining--;
        if (attempt.remaining <= 0) {
            switch (attempt.phase) {
                case NORMAL -> {
                    attempt.selected = chooseTwoColors(attempt.random);
                    attempt.phase = HazardPhase.WARNING; attempt.remaining = WARNING_TICKS;
                    warning = attempt.selected; countdown = 5;
                }
                case WARNING -> {
                    removeUnsafeFloor(level, attempt);
                    attempt.phase = HazardPhase.ABSENT; attempt.remaining = ABSENT_TICKS;
                    attempt.soundElapsed = 0; hazardSound = true;
                }
                case ABSENT -> {
                    restoreUnsafeFloor(level, attempt);
                    attempt.selected = List.of(); attempt.phase = HazardPhase.NORMAL; attempt.remaining = NORMAL_TICKS;
                    attempt.soundElapsed = -1;
                }
            }
        }
        return new TickResult(encounter(attempt), fallen, warning, countdown, playerFailY,
                attempt.floorY - 3, hazardSound);
    }

    public DeathResult onDeath(Zombie zombie) {
        GiantTier tier = tier(zombie);
        if (tier == null) return new DeathResult(false, false, null);
        Attempt attempt = attemptFor(zombie);
        if (attempt == null) return new DeathResult(false, false, null);
        attempt.giants.remove(zombie.getUUID());
        return new DeathResult(true, attempt.giants.isEmpty(), tier);
    }

    public boolean damageAllowed(Zombie zombie) {
        GiantTier target = tier(zombie);
        if (target == null) return true;
        Attempt attempt = attemptFor(zombie);
        if (attempt == null) return true;
        return weakestSurviving(attempt).map(value -> value == target).orElse(false);
    }

    private static Optional<GiantTier> weakestSurviving(Attempt attempt) {
        return weakestSurviving(attempt.giants.values());
    }

    public static Optional<GiantTier> weakestSurviving(Iterable<GiantTier> tiers) {
        GiantTier weakest = null;
        for (GiantTier tier : tiers) if (weakest == null || tier.compareTo(weakest) < 0) weakest = tier;
        return Optional.ofNullable(weakest);
    }

    public static boolean damageAllowed(GiantTier target, Iterable<GiantTier> surviving) {
        return weakestSurviving(surviving).map(tier -> tier == target).orElse(false);
    }

    private static void spawnGiant(ServerLevel level, TrialSession session, Attempt attempt, GiantTier tier) {
        Zombie zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.EVENT);
        if (zombie == null) throw new IllegalStateException("Could not create Warzone Giant");
        zombie.getAttribute(Attributes.SCALE).setBaseValue(GIANT_SCALE);
        zombie.refreshDimensions();
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(GIANT_HEALTH);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
        zombie.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0D);
        applyMovementAndDamage(zombie);
        zombie.setHealth((float) GIANT_HEALTH); zombie.setPersistenceRequired(); zombie.skipDropExperience();
        zombie.addEffect(new MobEffectInstance(MobEffects.STRENGTH, STRENGTH_TICKS,
                STRENGTH_AMPLIFIER, true, false, false));
        equip(level, zombie, tier, attempt.random);
        BlockPos spawn = findSpawn(level, session, attempt, zombie).orElseThrow(
                () -> new IllegalStateException("No collision-safe Warzone Giant spawn for " + tier));
        zombie.setPos(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D);
        zombie.addTag(ENCOUNTER_TAG); zombie.addTag(TIER_PREFIX + tier.serialized);
        zombie.addTag("cosmicpve_trial_session_" + session.sessionId());
        if (!level.addFreshEntity(zombie)) throw new IllegalStateException("Server rejected Warzone Giant " + tier);
        attempt.giants.put(zombie.getUUID(), tier);
    }

    public static void applyMovementAndDamage(Zombie zombie) {
        zombie.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                Zombie.createAttributes().build().getBaseValue(Attributes.MOVEMENT_SPEED) + GIANT_SPEED_INCREASE);
        zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(GIANT_ATTACK_DAMAGE);
    }

    private static Optional<BlockPos> findSpawn(ServerLevel level, TrialSession session, Attempt attempt, Zombie zombie) {
        var candidates = availableFloor(level, attempt);
        for (int tries = 0; tries < Math.min(256, candidates.size() * 2); tries++) {
            BlockPos floor = candidates.get(attempt.random.nextInt(candidates.size()));
            zombie.setPos(floor.getX() + 0.5D, floor.getY() + 1.0D, floor.getZ() + 0.5D);
            if (!level.noCollision(zombie)) continue;
            if (level.getEntities(zombie, zombie.getBoundingBox().inflate(0.5D), entity ->
                    entity instanceof ServerPlayer || entity.getTags().contains(ENCOUNTER_TAG)).isEmpty()) return Optional.of(floor);
        }
        return Optional.empty();
    }

    private static void recoverInvalidGiants(ServerLevel level, TrialSession session, Attempt attempt) {
        int threshold = attempt.floorY - 3;
        attempt.collisionTicks++;
        boolean inspectCollision = attempt.collisionTicks % COLLISION_RECOVERY_CADENCE == 0;
        for (UUID id : attempt.giants.keySet()) {
            Entity entity = level.getEntity(id);
            if (!(entity instanceof Zombie giant)) continue;
            if (giant.getY() <= threshold || inspectCollision && !level.noBlockCollision(giant, giant.getBoundingBox()))
                recoverGiant(level, session, attempt, giant);
        }
    }

    public boolean recoverFromSuffocation(ServerLevel level, TrialSession session, Zombie giant) {
        Attempt attempt = attemptFor(giant);
        return attempt != null && recoverGiant(level, session, attempt, giant);
    }

    private static boolean recoverGiant(ServerLevel level, TrialSession session, Attempt attempt, Zombie giant) {
        float health = giant.getHealth();
        BlockPos safe = findSpawn(level, session, attempt, giant).orElse(null);
        if (safe == null) return false;
        giant.setPos(safe.getX() + 0.5D, safe.getY() + 1.0D, safe.getZ() + 0.5D);
        giant.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); giant.fallDistance = 0.0F; giant.setHealth(health);
        return true;
    }

    private static void equip(ServerLevel level, Zombie zombie, GiantTier tier, RandomSource random) {
        var enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Item[] armor = tier.armor();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            ItemStack piece = new ItemStack(armor[i]); int protection = 1 + random.nextInt(4);
            EnchantmentHelper.updateEnchantments(piece, mutable -> mutable.set(enchants.getOrThrow(Enchantments.PROTECTION), protection));
            zombie.setItemSlot(slots[i], piece); zombie.setDropChance(slots[i], 0.0F);
        }
        ItemStack axe = new ItemStack(tier.axe);
        EnchantmentHelper.updateEnchantments(axe, mutable -> {
            mutable.set(enchants.getOrThrow(ModEnchantments.RAGE), 6);
            mutable.set(enchants.getOrThrow(ModEnchantments.BLEED), 6);
            if (tier.ordinal() >= GiantTier.CHAINMAIL.ordinal()) mutable.set(enchants.getOrThrow(ModEnchantments.PUMMEL), 3);
            if (tier.ordinal() >= GiantTier.DIAMOND.ordinal()) mutable.set(enchants.getOrThrow(ModEnchantments.SOUL_TETHER), 3);
        });
        zombie.setItemSlot(EquipmentSlot.MAINHAND, axe); zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    private static EnumMap<FloorColor, Map<BlockPos, BlockState>> indexFloor(ServerLevel level, InstanceBounds bounds) {
        var result = new EnumMap<FloorColor, Map<BlockPos, BlockState>>(FloorColor.class);
        for (FloorColor color : FloorColor.values()) result.put(color, new LinkedHashMap<>());
        for (BlockPos pos : BlockPos.betweenClosed(bounds.min(), bounds.max())) {
            FloorColor color = FloorColor.of(level.getBlockState(pos).getBlock());
            if (color != null) result.get(color).put(pos.immutable(), level.getBlockState(pos));
        }
        return result;
    }

    private static List<BlockPos> availableFloor(ServerLevel level, Attempt attempt) {
        return attempt.floor.values().stream().flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getValue().equals(level.getBlockState(entry.getKey())))
                .map(Map.Entry::getKey).toList();
    }

    private static void removeUnsafeFloor(ServerLevel level, Attempt attempt) {
        for (FloorColor color : unsafeColors(attempt.selected))
            attempt.floor.get(color).keySet().forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2));
    }

    private static void restoreUnsafeFloor(ServerLevel level, Attempt attempt) {
        for (FloorColor color : unsafeColors(attempt.selected))
            attempt.floor.get(color).forEach((pos, state) -> level.setBlock(pos, state, 2));
    }

    private static void restoreAllFloor(ServerLevel level, Attempt attempt) {
        for (FloorColor color : FloorColor.values())
            attempt.floor.get(color).forEach((pos, state) -> level.setBlock(pos, state, 2));
    }

    public void cleanup(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.remove(sessionId); if (attempt == null) return;
        restoreAllFloor(level, attempt);
        attempt.giants.keySet().forEach(id -> { Entity entity = level.getEntity(id); if (entity != null) entity.discard(); });
    }

    public String status(UUID sessionId) {
        Attempt a = attempts.get(sessionId);
        return a == null ? "Warzone state unavailable" : "warzone giants=" + a.giants.values()
                + " vulnerable=" + weakestSurviving(a).map(Enum::name).orElse("none")
                + " hazard=" + a.phase + ":" + a.remaining + " selected=" + a.selected;
    }

    public static int giantCount(int partySize) {
        if (partySize < 1 || partySize > 4) throw new IllegalArgumentException("party size must be 1-4");
        return partySize + 2;
    }

    public static List<GiantTier> selectTiers(int partySize, RandomSource random) {
        var tiers = new ArrayList<>(List.of(GiantTier.values()));
        for (int i = tiers.size() - 1; i > 0; i--) java.util.Collections.swap(tiers, i, random.nextInt(i + 1));
        return List.copyOf(tiers.subList(0, giantCount(partySize)));
    }

    public static List<FloorColor> chooseTwoColors(RandomSource random) {
        FloorColor first = FloorColor.values()[random.nextInt(FloorColor.values().length)];
        FloorColor second; do second = FloorColor.values()[random.nextInt(FloorColor.values().length)]; while (second == first);
        return List.of(first, second);
    }

    public static List<FloorColor> unsafeColors(List<FloorColor> safeColors) {
        return java.util.Arrays.stream(FloorColor.values()).filter(color -> !safeColors.contains(color)).toList();
    }

    public static boolean hazardSoundTick(int elapsed) { return elapsed == 0 || elapsed == 5 || elapsed == 10; }

    private Attempt attemptFor(Zombie giant) {
        for (Attempt attempt : attempts.values()) if (attempt.giants.containsKey(giant.getUUID())) return attempt;
        return null;
    }

    public static boolean encounterGiant(Zombie zombie) { return zombie.getTags().contains(ENCOUNTER_TAG); }
    public static GiantTier tier(Zombie zombie) {
        return zombie.getTags().stream().filter(tag -> tag.startsWith(TIER_PREFIX)).findFirst()
                .map(tag -> GiantTier.parse(tag.substring(TIER_PREFIX.length()))).orElse(null);
    }

    private static TrialEncounterState encounter(Attempt attempt) {
        return new TrialEncounterState(attempt.selected.stream().map(color -> color.serialized).toList(), attempt.remaining,
                attempt.tiers.stream().map(tier -> tier.serialized).toList(), List.of(), List.of(), List.of(),
                List.copyOf(attempt.giants.keySet()));
    }

    public enum HazardPhase { NORMAL, WARNING, ABSENT }
    public enum FloorColor {
        RED("red", Blocks.RED_WOOL, 0xB02E26), ORANGE("orange", Blocks.ORANGE_WOOL, 0xF9801D),
        YELLOW("yellow", Blocks.YELLOW_WOOL, 0xFED83D), LIME("lime", Blocks.LIME_WOOL, 0x80C71F),
        CYAN("cyan", Blocks.CYAN_WOOL, 0x169C9C), PURPLE("purple", Blocks.PURPLE_WOOL, 0x8932B8);
        public final String serialized; public final Block block; public final int color;
        FloorColor(String serialized, Block block, int color) { this.serialized=serialized; this.block=block; this.color=color; }
        static FloorColor of(Block block) { for (var value : values()) if (value.block == block) return value; return null; }
        public Component label() { return Component.literal(serialized.toUpperCase(java.util.Locale.ROOT))
                .withStyle(style -> style.withColor(color).withBold(true)); }
    }

    public enum GiantTier {
        LEATHER("leather", Items.WOODEN_AXE, new Item[]{Items.LEATHER_HELMET,Items.LEATHER_CHESTPLATE,Items.LEATHER_LEGGINGS,Items.LEATHER_BOOTS}),
        GOLD("gold", Items.GOLDEN_AXE, new Item[]{Items.GOLDEN_HELMET,Items.GOLDEN_CHESTPLATE,Items.GOLDEN_LEGGINGS,Items.GOLDEN_BOOTS}),
        CHAINMAIL("chainmail", Items.STONE_AXE, new Item[]{Items.CHAINMAIL_HELMET,Items.CHAINMAIL_CHESTPLATE,Items.CHAINMAIL_LEGGINGS,Items.CHAINMAIL_BOOTS}),
        IRON("iron", Items.IRON_AXE, new Item[]{Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS}),
        DIAMOND("diamond", Items.DIAMOND_AXE, new Item[]{Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS}),
        NETHERITE("netherite", Items.NETHERITE_AXE, new Item[]{Items.NETHERITE_HELMET,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.NETHERITE_BOOTS});
        public final String serialized; public final Item axe; private final Item[] armor;
        GiantTier(String serialized, Item axe, Item[] armor) { this.serialized=serialized; this.axe=axe; this.armor=armor; }
        public Item[] armor() { return armor.clone(); }
        static GiantTier parse(String id) { for (var tier : values()) if (tier.serialized.equals(id)) return tier; return null; }
    }

    public record TickResult(TrialEncounterState encounter, List<UUID> fallenPlayers, List<FloorColor> warningColors,
            int warningCountdown, int playerFailY, int giantRecoveryY, boolean hazardSound) {
        static final TickResult EMPTY = new TickResult(TrialEncounterState.EMPTY, List.of(), List.of(), 0, 0, 0, false);
    }
    public record DeathResult(boolean accepted, boolean complete, GiantTier tier) {}
    private static final class Attempt {
        final UUID sessionId; final InstanceBounds bounds; final EnumMap<FloorColor, Map<BlockPos, BlockState>> floor;
        final LinkedHashMap<UUID,GiantTier> giants; final List<GiantTier> tiers; HazardPhase phase; int remaining;
        List<FloorColor> selected; final RandomSource random; final int floorY;
        int collisionTicks; int soundElapsed = -1;
        Attempt(UUID sessionId, InstanceBounds bounds, EnumMap<FloorColor, Map<BlockPos, BlockState>> floor,
                LinkedHashMap<UUID,GiantTier> giants, List<GiantTier> tiers, HazardPhase phase, int remaining,
                List<FloorColor> selected, RandomSource random, int floorY) {
            this.sessionId=sessionId;this.bounds=bounds;this.floor=floor;this.giants=giants;this.tiers=tiers;
            this.phase=phase;this.remaining=remaining;this.selected=selected;this.random=random;this.floorY=floorY;
        }
    }
}
