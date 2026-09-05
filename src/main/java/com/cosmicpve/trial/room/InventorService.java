package com.cosmicpve.trial.room;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.action.CombatDeliveryScope;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.HeroicEquipmentKind;
import com.cosmicpve.entity.inventor.InventorEntity;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.equipment.heroic.HeroicApplicationService;
import com.cosmicpve.equipment.skin.WeaponSkinApplicationService;
import com.cosmicpve.equipment.skin.WeaponSkinDefinitions;
import com.cosmicpve.equipment.skin.WeaponSkinItemFactory;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModDamageTypes;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.trial.TrialSession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Runtime state for the production Inventor encounter. */
public final class InventorService {
    public static final BlockPos PLAYER_MARKER_LOCAL = new BlockPos(38, 2, 21);
    public static final BlockPos BOSS_MARKER_LOCAL = new BlockPos(4, 2, 21);
    public static final int ACTIVATION_MIN_TICKS = 300;
    public static final int ACTIVATION_MAX_TICKS = 400;
    public static final int HAZARD_INTERVAL_TICKS = 30;
    public static final double HAZARD_DAMAGE_PER_STATION = 1.5D;
    public static final int REQUIRED_INTERACTIONS = 3;
    public static final int SPEED_DURATION_TICKS = 100;

    public enum Station {
        BELL(new BlockPos(9,4,9), new BlockPos(7,4,7), Blocks.BELL),
        BUTTON(new BlockPos(33,4,9), new BlockPos(35,4,7), Blocks.POLISHED_BLACKSTONE_BUTTON),
        PLATE(new BlockPos(9,4,33), new BlockPos(7,4,35), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
        LEVER(new BlockPos(33,4,33), new BlockPos(35,4,35), Blocks.LEVER);

        public final BlockPos controlLocal;
        public final BlockPos beaconLocal;
        public final net.minecraft.world.level.block.Block controlBlock;
        Station(BlockPos controlLocal, BlockPos beaconLocal, net.minecraft.world.level.block.Block controlBlock) {
            this.controlLocal = controlLocal; this.beaconLocal = beaconLocal; this.controlBlock = controlBlock;
        }
    }

    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public void initialize(ServerLevel level, TrialSession session, BlockPos origin, InstanceBounds bounds, RandomSource random) {
        EnumMap<Station, StationState> stations = new EnumMap<>(Station.class);
        for (Station station : Station.values()) {
            BlockPos control = origin.offset(station.controlLocal);
            BlockPos beacon = origin.offset(station.beaconLocal);
            if (!bounds.contains(control) || !level.getBlockState(control).is(station.controlBlock))
                throw new IllegalStateException("Inventor structure is missing " + station + " control at " + control);
            if (!bounds.contains(beacon) || !level.getBlockState(beacon).is(Blocks.BEACON))
                throw new IllegalStateException("Inventor structure is missing " + station + " Beacon at " + beacon);
            boolean powered = station == Station.PLATE && powered(level.getBlockState(control));
            stations.put(station, new StationState(control, beacon, false, 0, powered));
        }
        stations.values().forEach(state -> level.setBlock(state.beacon, Blocks.AIR.defaultBlockState(), 3));
        BlockPos bossMarker = origin.offset(BOSS_MARKER_LOCAL);
        if (!level.getBlockState(bossMarker).is(Blocks.DIAMOND_BLOCK))
            throw new IllegalStateException("Inventor boss marker is missing at " + bossMarker);
        level.setBlock(bossMarker, InstanceStructureService.inferredFloorReplacement(level, bossMarker, "Inventor boss marker"), 3);
        InventorEntity boss = spawnBoss(level, session, bossMarker.above());
        attempts.put(session.sessionId(), new Attempt(session.sessionId(), bounds, origin, stations, boss.getUUID(), random,
                nextActivation(random), HAZARD_INTERVAL_TICKS));
    }

    public void activate(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return;
        inventor(level, attempt).ifPresent(boss -> boss.setNoAi(false));
    }

    public void tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return;
        if (--attempt.activationTicks <= 0) {
            boolean activated = activateRandomStation(attempt);
            attempt.activationTicks = nextActivation(attempt.random);
            syncBeacons(level, attempt);
            syncBoss(level, attempt);
            if (activated) announceBeacon(level, session);
        }
        if (--attempt.hazardTicks <= 0) {
            attempt.hazardTicks = HAZARD_INTERVAL_TICKS;
            double amount = hazardDamage(activeCount(attempt));
            if (amount > 0.0D) for (UUID participant : session.participants()) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
                if (player != null && !player.isDeadOrDying()) deliverHazard(level, player, amount);
            }
        }
    }

    public boolean interact(ServerPlayer player, BlockPos pos) {
        Attempt attempt = attemptFor(player);
        if (attempt == null) return false;
        for (var entry : attempt.stations.entrySet()) {
            Station station = entry.getKey(); StationState state = entry.getValue();
            if (station == Station.PLATE || !state.control.equals(pos) || !state.active) continue;
            BlockState block = player.level().getBlockState(pos);
            if (station == Station.BUTTON && block.hasProperty(ButtonBlock.POWERED) && block.getValue(ButtonBlock.POWERED)) return false;
            completeInteraction(player, attempt, station, state); return true;
        }
        return false;
    }

    public boolean plate(ServerLevel level, TrialSession session, BlockPos pos, BlockState block) {
        Attempt attempt = attempts.values().stream().filter(value -> value.stations.get(Station.PLATE).control.equals(pos)).findFirst().orElse(null);
        if (attempt == null) return false;
        StationState old = attempt.stations.get(Station.PLATE);
        boolean now = powered(block);
        attempt.stations.put(Station.PLATE, old.withPowered(now));
        if (!old.powered && now && old.active) {
            ServerPlayer activator = level.getEntitiesOfClass(ServerPlayer.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(0.5D)).stream()
                    .filter(player -> session.activeParticipant(player.getUUID())).findFirst().orElse(null);
            if (activator != null) completeInteraction(activator, attempt, Station.PLATE, old.withPowered(now));
            return activator != null;
        }
        return false;
    }

    private void completeInteraction(ServerPlayer player, Attempt attempt, Station station, StationState state) {
        int progress = state.progress + 1;
        if (progress < REQUIRED_INTERACTIONS) {
            attempt.stations.put(station, state.withProgress(progress));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(station.name() + " workstation: "
                    + progress + "/" + REQUIRED_INTERACTIONS));
            return;
        }
        attempt.stations.put(station, new StationState(state.control, state.beacon, false, 0, state.powered));
        InventorEntity boss = inventor((ServerLevel) player.level(), attempt).orElse(null);
        if (boss != null) {
            boss.setActiveStations(activeCount(attempt));
            boss.grantStrikeCharge(player.getUUID());
        }
        syncBeacons((ServerLevel) player.level(), attempt);
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, SPEED_DURATION_TICKS, 0, true, false, true));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(station.name() + " workstation shut down!"));
    }

    public boolean forceActivation(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return false;
        boolean changed = activateRandomStation(attempt);
        syncBeacons(level, attempt);
        syncBoss(level, attempt);
        if (changed) announceBeacon(level, session);
        return changed;
    }

    static boolean activateRandomStation(Attempt attempt) {
        var inactive = java.util.Arrays.stream(Station.values()).filter(station -> !attempt.stations.get(station).active).toList();
        if (inactive.isEmpty()) return false;
        Station chosen = inactive.get(attempt.random.nextInt(inactive.size()));
        StationState old = attempt.stations.get(chosen);
        attempt.stations.put(chosen, new StationState(old.control, old.beacon, true, 0, old.powered));
        return true;
    }

    public boolean bossDeath(InventorEntity boss) {
        Attempt attempt = attempts.values().stream().filter(value -> value.bossId.equals(boss.getUUID())).findFirst().orElse(null);
        return attempt != null;
    }

    public void removeParticipantCharge(ServerLevel level, UUID sessionId, UUID player) {
        Attempt attempt = attempts.get(sessionId);
        if (attempt != null) inventor(level, attempt).ifPresent(boss -> boss.consumeStrikeCharge(player));
    }

    public void cleanup(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.remove(sessionId);
        if (attempt == null) return;
        inventor(level, attempt).ifPresent(boss -> { boss.clearStrikeCharges(); boss.discard(); });
    }

    public String status(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.get(sessionId);
        if (attempt == null) return "Inventor state unavailable";
        InventorEntity boss = inventor(level, attempt).orElse(null);
        String bossState = boss == null ? "missing" : boss.getHealth() + "/" + boss.getMaxHealth() + " HP";
        String charges = boss == null ? "[]" : boss.strikeCharges().toString();
        return "inventor boss=" + bossState + " charged=" + charges + " active=" + activeCount(attempt)
                + " next=" + attempt.activationTicks + "t hazard="
                + attempt.hazardTicks + "t stations=" + attempt.stations.entrySet().stream()
                .map(entry -> entry.getKey().name().toLowerCase() + ":" + (entry.getValue().active ? "active" : "off")
                        + ":" + entry.getValue().progress + "/3").toList();
    }

    public InventorEntity spawnBoss(ServerLevel level, TrialSession session, BlockPos spawn) {
        InventorEntity boss = ModEntities.INVENTOR.get().create(level, EntitySpawnReason.EVENT);
        if (boss == null) throw new IllegalStateException("Could not create Inventor");
        double health = bossHealth(session.participants().size());
        boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        boss.setHealth((float) health); boss.setPersistenceRequired(); boss.skipDropExperience(); boss.setNoAi(true);
        equipBoss(boss, level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), session.participants().size());
        boss.setPos(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        boss.addTag("cosmicpve_trial_inventor"); boss.addTag("cosmicpve_trial_session_" + session.sessionId());
        if (!level.addFreshEntity(boss)) throw new IllegalStateException("Server rejected Inventor");
        return boss;
    }

    public static void equipBoss(InventorEntity boss, Registry<Enchantment> registry, int partySize) {
        boss.setItemSlot(EquipmentSlot.HEAD, bossArmor(Items.IRON_HELMET, registry,
                Map.of(ModEnchantments.MORTAL_COIL, 2, ModEnchantments.ANGELIC, 5)));
        boss.setItemSlot(EquipmentSlot.CHEST, bossArmor(Items.IRON_CHESTPLATE, registry,
                Map.of(ModEnchantments.PERMAFROST, 6, ModEnchantments.STORMCALLER, 5, ModEnchantments.ANGELIC, 5)));
        boss.setItemSlot(EquipmentSlot.LEGS, bossArmor(Items.IRON_LEGGINGS, registry,
                Map.of(ModEnchantments.LUCK, 10, ModEnchantments.ANGELIC, 5, ModEnchantments.PLAGUE_CARRIER, 7)));
        boss.setItemSlot(EquipmentSlot.FEET, bossArmor(Items.IRON_BOOTS, registry,
                Map.of(ModEnchantments.LUCK, 10, ModEnchantments.DODGE, 5, ModEnchantments.STORMCALLER, 5)));
        ItemStack axe = new ItemStack(bossAxe(partySize));
        EnchantmentHelper.updateEnchantments(axe, mutable -> {
            set(mutable, registry, ModEnchantments.SOUL_SIPHON, 4); set(mutable, registry, ModEnchantments.SOUL_TETHER, 3);
            set(mutable, registry, ModEnchantments.DEEP_BLEED, 6); set(mutable, registry, ModEnchantments.INSANITY, 8);
            set(mutable, registry, ModEnchantments.RAGE, 6); set(mutable, registry, ModEnchantments.BLESSED, 4);
            set(mutable, registry, ModEnchantments.PUMMEL, 3);
        });
        axe.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), CustomEnchantMetadata.DEFAULT.withOrbUpgrades(5));
        ItemStack skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.STORMBRINGER);
        if (new WeaponSkinApplicationService().apply(skin, axe, skin, axe) != WeaponSkinApplicationService.ApplyOutcome.SUCCESS)
            throw new IllegalStateException("Could not attach Stormbringer to Inventor axe");
        boss.setItemSlot(EquipmentSlot.MAINHAND, axe);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND}) boss.setDropChance(slot, 0.0F);
    }

    private static ItemStack bossArmor(net.minecraft.world.item.Item item, Registry<Enchantment> registry,
            Map<net.minecraft.resources.ResourceKey<Enchantment>, Integer> enchantments) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.ARMOR_SET_ID.get(), ArmorSetIdentity.from(
                com.cosmicpve.content.CosmicContent.repository().requireArmorSetDefinition(ArmorSetIds.ENGINEER)));
        HeroicApplicationService.applyState(stack, HeroicEquipmentKind.ARMOR);
        EnchantmentHelper.updateEnchantments(stack, mutable -> enchantments.forEach((key, level) -> set(mutable, registry, key, level)));
        return stack;
    }

    private static void set(net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable,
            Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key, int level) {
        mutable.set(registry.getOrThrow(key), level);
    }

    private static void deliverHazard(ServerLevel level, ServerPlayer target, double amount) {
        var sequence = CosmicCombat.sequences().nextRoot();
        var source = level.damageSources().source(ModDamageTypes.TRUE_DAMAGE);
        var context = new CombatContext(null, null, null, target, java.util.Optional.empty(), source,
                AttackCategory.ENVIRONMENTAL, DamageChannel.TRUE, java.util.Set.of(),
                com.cosmicpve.combat.api.WeaponSnapshot.empty(),
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                sequence.id(), sequence.parentId(), RecursionPolicy.NO_PROCS);
        TrueDamagePacket packet = TrueDamagePacket.standard(CosmicPVE.id("trial/inventor_hazard"), amount);
        CombatDeliveryScope.call(context, packet, () -> target.hurtServer(level, source, (float) amount));
    }

    private Attempt attemptFor(ServerPlayer player) {
        return attempts.values().stream().filter(attempt -> attempt.bounds.contains(player.blockPosition())).findFirst().orElse(null);
    }
    private static boolean powered(BlockState state) {
        return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER)
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) > 0;
    }
    private static int nextActivation(RandomSource random) {
        return ACTIVATION_MIN_TICKS + random.nextInt(ACTIVATION_MAX_TICKS - ACTIVATION_MIN_TICKS + 1);
    }
    public static double hazardDamage(int activeStations) {
        return Math.max(0, Math.min(4, activeStations)) * HAZARD_DAMAGE_PER_STATION;
    }
    public static double bossHealth(int partySize) {
        if (partySize < 1 || partySize > 4) throw new IllegalArgumentException("party size must be 1-4");
        return 250.0D + (partySize - 1) * 200.0D;
    }
    public static net.minecraft.world.item.Item bossAxe(int partySize) {
        if (partySize < 1 || partySize > 4) throw new IllegalArgumentException("party size must be 1-4");
        return partySize <= 2 ? Items.DIAMOND_AXE : Items.NETHERITE_AXE;
    }
    private static int activeCount(Attempt attempt) {
        return (int) attempt.stations.values().stream().filter(value -> value.active).count();
    }
    private static java.util.Optional<InventorEntity> inventor(ServerLevel level, Attempt attempt) {
        Entity entity = level.getEntity(attempt.bossId); return entity instanceof InventorEntity boss ? java.util.Optional.of(boss) : java.util.Optional.empty();
    }
    private static void syncBoss(ServerLevel level, Attempt attempt) {
        inventor(level, attempt).ifPresent(boss -> boss.setActiveStations(activeCount(attempt)));
    }
    private static void syncBeacons(ServerLevel level, Attempt attempt) {
        attempt.stations.values().forEach(state -> level.setBlock(state.beacon,
                state.active ? Blocks.BEACON.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3));
    }

    private static void announceBeacon(ServerLevel level, TrialSession session) {
        for (UUID participant : session.participants()) {
            if (!session.activeParticipant(participant)) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null) player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "message.cosmicpve.trial.inventor.beacon_spawned"));
        }
    }

    static final class Attempt {
        final UUID sessionId; final InstanceBounds bounds; final BlockPos origin; final EnumMap<Station, StationState> stations;
        final UUID bossId; final RandomSource random; int activationTicks; int hazardTicks;
        Attempt(UUID sessionId, InstanceBounds bounds, BlockPos origin, EnumMap<Station, StationState> stations,
                UUID bossId, RandomSource random, int activationTicks, int hazardTicks) {
            this.sessionId=sessionId; this.bounds=bounds; this.origin=origin; this.stations=stations; this.bossId=bossId;
            this.random=random; this.activationTicks=activationTicks; this.hazardTicks=hazardTicks;
        }
    }
    record StationState(BlockPos control, BlockPos beacon, boolean active, int progress, boolean powered) {
        StationState withProgress(int value) { return new StationState(control, beacon, active, value, powered); }
        StationState withPowered(boolean value) { return new StationState(control, beacon, active, progress, value); }
    }
}
