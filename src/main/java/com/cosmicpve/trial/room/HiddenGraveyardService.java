package com.cosmicpve.trial.room;

import com.cosmicpve.data.component.HiddenGraveyardKeyData;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEquipmentService;
import com.cosmicpve.instance.InstanceBounds;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.registry.ModEntities;
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Runtime for the production Hidden Graveyard room. All authored marker/grave positions are local and bounded. */
public final class HiddenGraveyardService {
    public static final BlockPos SPAWN_MARKER_LOCAL = new BlockPos(14, 7, 39);
    public static final BlockPos WELL_LOCAL = new BlockPos(15, 7, 13);
    public static final List<BlockPos> CHEST_MARKERS_LOCAL = List.of(
            new BlockPos(14,7,6), new BlockPos(26,7,10), new BlockPos(23,7,16), new BlockPos(31,7,16),
            new BlockPos(11,7,17), new BlockPos(3,7,23), new BlockPos(15,7,24), new BlockPos(22,7,24),
            new BlockPos(34,7,28), new BlockPos(8,7,30), new BlockPos(26,7,37));
    public static final int CORPSES_PER_WAVE = 6;
    public static final int KEY_KILL_THRESHOLD = 3;
    public static final int WAVE_COUNT = 3;
    private static final List<List<BlockPos>> COBBLESTONE_GRAVES = graves(
            grave(18,16,20,16), grave(27,16,29,16), grave(8,17,7,17),
            grave(10,23,9,23), grave(18,25,20,25), grave(29,30,31,30));
    private static final List<List<BlockPos>> ANDESITE_GRAVES = graves(
            graveTurned(16,6,16,5), graveTurned(20,6,20,5), graveTurned(24,6,24,5),
            grave(6,27,5,27), grave(18,29,20,29), grave(10,33,9,33));
    private static final List<List<BlockPos>> STONE_GRAVES = graves(
            grave(10,7,9,7), graveTurned(30,9,30,8), grave(9,13,8,13),
            grave(4,20,3,20), grave(31,22,33,22), graveTurned(22,34,22,36));

    private final Map<UUID, Attempt> attempts = new HashMap<>();

    public Initialization initialize(ServerLevel level, TrialSession session, BlockPos origin,
            InstanceBounds bounds, RandomSource random) {
        UUID attemptId = UUID.randomUUID();
        var attempt = new Attempt(session.sessionId(), attemptId, bounds, origin, random,
                new int[WAVE_COUNT], new HashMap<>(), new LinkedHashSet<>(), 1, 0, null, null,
                session.participants().size());
        attempts.put(session.sessionId(), attempt);
        applyPaleGardenBiome(level, bounds);
        issueKeyChest(level, attempt, 1);
        TrialEncounterState state = encounter(attempt);
        return new Initialization(state, attemptId);
    }

    public void tick(ServerLevel level, TrialSession session) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null) return;
        removeEmptyChest(level, attempt);
        consumeWellKey(level, session, attempt);
        if (attempt.expectedKey() > 0 && !hasCurrentKey(level, session, attempt)) issueKeyChest(level, attempt, attempt.expectedKey());
    }

    private void consumeWellKey(ServerLevel level, TrialSession session, Attempt attempt) {
        BlockPos center = attempt.origin().offset(WELL_LOCAL);
        AABB well = new AABB(center.getX()-1, center.getY()-3, center.getZ()-1,
                center.getX()+2, center.getY()+3, center.getZ()+2);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, well)) {
            if (!validKey(item.getItem(), attempt, attempt.expectedKey())) continue;
            int wave = attempt.expectedKey();
            item.getItem().shrink(1); if (item.getItem().isEmpty()) item.discard();
            attempt.expectedKey(0); attempt.wave(wave);
            spawnWave(level, session, attempt, wave);
            announceWave(level, session, wave);
            break;
        }
    }

    public DeathResult onDeath(ServerLevel level, TrialSession session, UndeadCorpseEntity corpse) {
        Attempt attempt = attempts.get(session.sessionId());
        if (attempt == null || !corpse.trialEncounter(session.sessionId(), attempt.attemptId())
                || attempt.corpses().remove(corpse.getUUID()) == null) return DeathResult.ignored();
        int wave = corpse.trialWave();
        if (wave < 1 || wave > WAVE_COUNT) return DeathResult.ignored();
        int kills = ++attempt.kills()[wave - 1];
        boolean keyIssued = false;
        if (shouldIssueNextKey(wave, kills) && attempt.expectedKey() == 0) {
            attempt.expectedKey(wave + 1); issueKeyChest(level, attempt, wave + 1); keyIssued = true;
        }
        boolean complete = completionReady(attempt.wave(), attempt.corpses().size());
        return new DeathResult(true, wave, kills, keyIssued, complete, encounter(attempt));
    }

    private void spawnWave(ServerLevel level, TrialSession session, Attempt attempt, int wave) {
        List<List<BlockPos>> graves = wave == 1 ? COBBLESTONE_GRAVES : wave == 2 ? ANDESITE_GRAVES : STONE_GRAVES;
        for (List<BlockPos> grave : graves) {
            BlockPos spawn = graveSpawn(attempt.origin(), grave);
            for (BlockPos local : grave) level.setBlock(attempt.origin().offset(local), Blocks.AIR.defaultBlockState(), 3);
            spawnCorpse(level, session, attempt, wave, spawn);
            if (attempt.random().nextDouble() < doubleCorpseChance(attempt.partySize()))
                spawnCorpse(level, session, attempt, wave, nearbySpawn(level, spawn));
        }
    }

    private static void spawnCorpse(ServerLevel level, TrialSession session, Attempt attempt, int wave, BlockPos spawn) {
        UndeadCorpseEntity corpse = ModEntities.UNDEAD_CORPSE.get().create(level, EntitySpawnReason.EVENT);
        if (corpse == null) throw new IllegalStateException("Could not create Undead Corpse");
        corpse.setPos(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        UndeadCorpseEquipmentService.equipBase(corpse, level.registryAccess(), attempt.random());
        equipWave(corpse, level, wave, attempt.random());
        corpse.markTrialEncounter(session.sessionId(), attempt.attemptId(), wave);
        corpse.setHealth((float)UndeadCorpseEntity.MAX_HEALTH);
        if (!level.addFreshEntity(corpse)) throw new IllegalStateException("Server rejected Hidden Graveyard corpse");
        attempt.corpses().put(corpse.getUUID(), wave);
    }

    private static BlockPos nearbySpawn(ServerLevel level, BlockPos primary) {
        for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos candidate = primary.relative(direction);
            if (level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()
                    && level.getBlockState(candidate.above()).getCollisionShape(level, candidate.above()).isEmpty()) return candidate;
        }
        return primary.above();
    }

    public static double doubleCorpseChance(int partySize) {
        if (partySize < 1 || partySize > 4) throw new IllegalArgumentException("party size must be 1-4");
        return 0.2D * (partySize - 1);
    }

    private static void equipWave(UndeadCorpseEntity corpse, ServerLevel level, int wave, RandomSource random) {
        WaveProfile profile = waveProfile(wave);
        if (wave == 1) {
            equipPlain(corpse, EquipmentSlot.HEAD, Items.LEATHER_HELMET);
            equipPlain(corpse, EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE);
            equipPlain(corpse, EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS);
            equipPlain(corpse, EquipmentSlot.FEET, Items.LEATHER_BOOTS); return;
        }
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Item[] pieces = profile.armorTier() == ArmorTier.CHAINMAIL
                ? new Item[]{Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS}
                : new Item[]{Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            EquipmentSlot slot = slots[i]; ItemStack stack = new ItemStack(pieces[i]);
            int protection = profile.protectionLevel();
            EnchantmentHelper.updateEnchantments(stack, mutable -> {
                mutable.set(registry.getOrThrow(Enchantments.PROTECTION), protection);
                if ((slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET)) {
                    int luck = profile.luckMinimum() + random.nextInt(profile.luckMaximum()-profile.luckMinimum()+1);
                    mutable.set(registry.getOrThrow(ModEnchantments.LUCK), luck);
                }
                if (wave == 3 && slot == EquipmentSlot.CHEST)
                    mutable.set(registry.getOrThrow(ModEnchantments.AEGIS), profile.aegisMinimum()
                            + random.nextInt(profile.aegisMaximum()-profile.aegisMinimum()+1));
            });
            corpse.setItemSlot(slot, stack); corpse.setDropChance(slot, 0.0F);
        }
    }

    private static void equipPlain(UndeadCorpseEntity corpse, EquipmentSlot slot, Item item) {
        corpse.setItemSlot(slot, new ItemStack(item)); corpse.setDropChance(slot, 0.0F);
    }

    private void issueKeyChest(ServerLevel level, Attempt attempt, int sequence) {
        removeEmptyChest(level, attempt);
        if (attempt.activeChest() != null) return;
        List<BlockPos> available = CHEST_MARKERS_LOCAL.stream().map(attempt.origin()::offset)
                .filter(pos -> !attempt.usedChestMarkers().contains(pos)).toList();
        if (available.isEmpty()) {
            attempt.usedChestMarkers().clear();
            available = CHEST_MARKERS_LOCAL.stream().map(attempt.origin()::offset).toList();
        }
        BlockPos marker = available.get(attempt.random().nextInt(available.size()));
        BlockState replacement = InstanceStructureService.inferredFloorReplacement(level, marker, "Hidden Graveyard key chest");
        attempt.chestReplacement(replacement); attempt.activeChest(marker); attempt.usedChestMarkers().add(marker);
        level.setBlock(marker, Blocks.CHEST.defaultBlockState(), 3);
        if (!(level.getBlockEntity(marker) instanceof ChestBlockEntity chest))
            throw new IllegalStateException("Hidden Graveyard key chest failed to initialize at " + marker);
        chest.setItem(13, createKey(attempt.sessionId(), attempt.attemptId(), sequence)); chest.setChanged();
    }

    private static ItemStack createKey(UUID session, UUID attempt, int sequence) {
        ItemStack key = new ItemStack(Items.TRIAL_KEY);
        key.set(ModDataComponents.HIDDEN_GRAVEYARD_KEY.get(), new HiddenGraveyardKeyData(session, attempt, sequence));
        key.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal("Hidden Graveyard Key " + sequence));
        return key;
    }
    public static ItemStack key(UUID session, UUID attempt, int sequence) { return createKey(session, attempt, sequence); }
    public static boolean validKey(ItemStack stack, Attempt attempt, int sequence) {
        HiddenGraveyardKeyData data = stack.get(ModDataComponents.HIDDEN_GRAVEYARD_KEY.get());
        return stack.is(Items.TRIAL_KEY) && data != null && data.valid() && data.sessionId().equals(attempt.sessionId())
                && data.attemptId().equals(attempt.attemptId()) && data.sequence() == sequence;
    }

    private static boolean hasCurrentKey(ServerLevel level, TrialSession session, Attempt attempt) {
        int sequence = attempt.expectedKey(); if (sequence <= 0) return true;
        if (attempt.activeChest() != null && level.getBlockEntity(attempt.activeChest()) instanceof Container chest)
            for (int i = 0; i < chest.getContainerSize(); i++) if (validKey(chest.getItem(i), attempt, sequence)) return true;
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.getInventory().contains(stack -> validKey(stack, attempt, sequence))) return true;
        }
        return !level.getEntitiesOfClass(ItemEntity.class, new AABB(attempt.bounds().min().getX(), attempt.bounds().min().getY(),
                attempt.bounds().min().getZ(), attempt.bounds().max().getX() + 1, attempt.bounds().max().getY() + 1,
                attempt.bounds().max().getZ() + 1), item -> validKey(item.getItem(), attempt, sequence)).isEmpty();
    }

    private static void removeEmptyChest(ServerLevel level, Attempt attempt) {
        BlockPos pos = attempt.activeChest(); if (pos == null) return;
        if (level.getBlockEntity(pos) instanceof Container chest && !chest.isEmpty()) return;
        level.setBlock(pos, attempt.chestReplacement() == null ? Blocks.SOUL_SAND.defaultBlockState() : attempt.chestReplacement(), 3);
        attempt.activeChest(null); attempt.chestReplacement(null);
    }

    private static void announceWave(ServerLevel level, TrialSession session, int wave) {
        for (UUID id : session.participants()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id); if (player == null) continue;
            player.sendSystemMessage(Component.literal("Wave " + wave + " has spawned!"));
            player.playSound(SoundEvents.END_PORTAL_SPAWN, 4.0F, 1.0F);
        }
    }

    private static TrialEncounterState encounter(Attempt attempt) {
        return new TrialEncounterState(List.of("wave=" + attempt.wave(), "expected_key=" + attempt.expectedKey()),
                java.util.Arrays.stream(attempt.kills()).sum(), List.of(), List.of(), List.of(),
                attempt.activeChest() == null ? List.of() : List.of(attempt.activeChest()), List.copyOf(attempt.corpses().keySet()));
    }

    public String status(UUID sessionId) {
        Attempt a = attempts.get(sessionId); if (a == null) return "hidden graveyard state unavailable";
        int[] alive = new int[WAVE_COUNT]; a.corpses().values().forEach(wave -> alive[wave-1]++);
        return "attempt=" + a.attemptId() + " wave=" + a.wave() + " kills=" + java.util.Arrays.toString(a.kills())
                + " alive=" + java.util.Arrays.toString(alive) + " expectedKey=" + a.expectedKey()
                + " chest=" + a.activeChest() + " tracked=" + a.corpses().size();
    }
    public boolean activeChest(UUID sessionId, BlockPos pos) {
        Attempt attempt = attempts.get(sessionId);
        return attempt != null && pos.equals(attempt.activeChest());
    }
    public static List<List<BlockPos>> authoredGraves(int wave) {
        return wave == 1 ? COBBLESTONE_GRAVES : wave == 2 ? ANDESITE_GRAVES
                : wave == 3 ? STONE_GRAVES : List.of();
    }
    public static boolean shouldIssueNextKey(int wave, int waveKills) {
        return wave >= 1 && wave < WAVE_COUNT && waveKills == KEY_KILL_THRESHOLD;
    }
    public static boolean completionReady(int highestSummonedWave, int trackedAlive) {
        return highestSummonedWave == WAVE_COUNT && trackedAlive == 0;
    }
    public static WaveProfile waveProfile(int wave) {
        return switch (wave) {
            case 1 -> new WaveProfile(ArmorTier.LEATHER,0,0,0,0,0);
            case 2 -> new WaveProfile(ArmorTier.CHAINMAIL,1,1,10,0,0);
            case 3 -> new WaveProfile(ArmorTier.IRON,2,5,10,1,6);
            default -> throw new IllegalArgumentException("wave must be 1-3");
        };
    }

    public void cleanup(ServerLevel level, UUID sessionId) {
        Attempt attempt = attempts.remove(sessionId); if (attempt == null) return;
        for (UUID id : attempt.corpses().keySet()) { Entity entity = level.getEntity(id); if (entity != null) entity.discard(); }
        AABB bounds = new AABB(attempt.bounds().min().getX(), attempt.bounds().min().getY(), attempt.bounds().min().getZ(),
                attempt.bounds().max().getX()+1, attempt.bounds().max().getY()+1, attempt.bounds().max().getZ()+1);
        level.getEntitiesOfClass(ItemEntity.class, bounds, item -> {
            HiddenGraveyardKeyData data = item.getItem().get(ModDataComponents.HIDDEN_GRAVEYARD_KEY.get());
            return data != null && data.attemptId().equals(attempt.attemptId());
        }).forEach(ItemEntity::discard);
        setBiome(level, attempt.bounds(), Biomes.THE_VOID);
    }

    private static void applyPaleGardenBiome(ServerLevel level, InstanceBounds bounds) {
        setBiome(level, bounds, Biomes.PALE_GARDEN);
    }
    private static void setBiome(ServerLevel level, InstanceBounds bounds,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome) {
        Holder<net.minecraft.world.level.biome.Biome> pale = level.registryAccess().lookupOrThrow(Registries.BIOME)
                .getOrThrow(biome);
        int minQuartY = net.minecraft.core.QuartPos.fromBlock(bounds.min().getY());
        int maxQuartY = net.minecraft.core.QuartPos.fromBlock(bounds.max().getY());
        int minQuartX = net.minecraft.core.QuartPos.fromBlock(bounds.min().getX());
        int maxQuartX = net.minecraft.core.QuartPos.fromBlock(bounds.max().getX());
        int minQuartZ = net.minecraft.core.QuartPos.fromBlock(bounds.min().getZ());
        int maxQuartZ = net.minecraft.core.QuartPos.fromBlock(bounds.max().getZ());
        for (int chunkX = bounds.min().getX() >> 4; chunkX <= bounds.max().getX() >> 4; chunkX++)
            for (int chunkZ = bounds.min().getZ() >> 4; chunkZ <= bounds.max().getZ() >> 4; chunkZ++) {
                var chunk = level.getChunk(chunkX, chunkZ);
                for (int quartY = minQuartY; quartY <= maxQuartY; quartY++) {
                    var section = chunk.getSection(chunk.getSectionIndex(net.minecraft.core.QuartPos.toBlock(quartY)));
                    @SuppressWarnings("unchecked") var biomes = (net.minecraft.world.level.chunk.PalettedContainer<Holder<net.minecraft.world.level.biome.Biome>>)
                            section.getBiomes();
                    int localY = quartY & 3;
                    for (int quartX=Math.max(minQuartX,chunkX<<2);quartX<=Math.min(maxQuartX,(chunkX<<2)+3);quartX++)
                        for (int quartZ=Math.max(minQuartZ,chunkZ<<2);quartZ<=Math.min(maxQuartZ,(chunkZ<<2)+3);quartZ++)
                            biomes.set(quartX&3, localY, quartZ&3, pale);
                }
                chunk.markUnsaved();
            }
    }

    private static List<List<BlockPos>> graves(List<BlockPos>... groups) { return List.of(groups); }
    private static List<BlockPos> grave(int slabX, int slabZ, int stairX, int stairZ) {
        var result = new ArrayList<BlockPos>();
        for (int x = slabX; x <= slabX + 1; x++) for (int z = slabZ; z <= slabZ + 1; z++) result.add(new BlockPos(x,8,z));
        for (int y=8; y<=10; y++) for (int z=stairZ; z<=stairZ+1; z++) result.add(new BlockPos(stairX,y,z));
        return List.copyOf(result);
    }
    private static List<BlockPos> graveTurned(int slabX, int slabZ, int stairX, int stairZ) {
        var result = new ArrayList<BlockPos>();
        for (int x=slabX; x<=slabX+1; x++) for (int z=slabZ; z<=slabZ+1; z++) result.add(new BlockPos(x,8,z));
        for (int y=8; y<=10; y++) for (int x=stairX; x<=stairX+1; x++) result.add(new BlockPos(x,y,stairZ));
        return List.copyOf(result);
    }
    private static BlockPos graveSpawn(BlockPos origin, List<BlockPos> grave) {
        int x=(int)Math.round(grave.stream().mapToInt(BlockPos::getX).average().orElseThrow());
        int z=(int)Math.round(grave.stream().mapToInt(BlockPos::getZ).average().orElseThrow());
        return origin.offset(x, 8, z);
    }

    public record Initialization(TrialEncounterState encounter, UUID attemptId) {}
    public enum ArmorTier { LEATHER, CHAINMAIL, IRON }
    public record WaveProfile(ArmorTier armorTier, int protectionLevel, int luckMinimum, int luckMaximum,
            int aegisMinimum, int aegisMaximum) {}
    public record DeathResult(boolean accepted, int wave, int kills, boolean keyIssued, boolean complete,
            TrialEncounterState encounter) { static DeathResult ignored() { return new DeathResult(false,0,0,false,false,TrialEncounterState.EMPTY); } }
    public static final class Attempt {
        private final UUID sessionId, attemptId; private final InstanceBounds bounds; private final BlockPos origin;
        private final RandomSource random; private final int[] kills; private final Map<UUID,Integer> corpses;
        private final Set<BlockPos> usedChestMarkers; private int expectedKey, wave; private BlockPos activeChest;
        private BlockState chestReplacement; private final int partySize;
        Attempt(UUID sessionId, UUID attemptId, InstanceBounds bounds, BlockPos origin, RandomSource random, int[] kills,
                Map<UUID,Integer> corpses, Set<BlockPos> usedChestMarkers, int expectedKey, int wave, BlockPos activeChest,
                BlockState chestReplacement, int partySize) { this.sessionId=sessionId; this.attemptId=attemptId; this.bounds=bounds;
            this.origin=origin; this.random=random; this.kills=kills; this.corpses=corpses; this.usedChestMarkers=usedChestMarkers;
            this.expectedKey=expectedKey; this.wave=wave; this.activeChest=activeChest; this.chestReplacement=chestReplacement;
            this.partySize=partySize; }
        public UUID sessionId(){return sessionId;} public UUID attemptId(){return attemptId;} public InstanceBounds bounds(){return bounds;}
        public BlockPos origin(){return origin;} public RandomSource random(){return random;} public int[] kills(){return kills;}
        public Map<UUID,Integer> corpses(){return corpses;} public Set<BlockPos> usedChestMarkers(){return usedChestMarkers;}
        public int expectedKey(){return expectedKey;} void expectedKey(int value){expectedKey=value;} public int wave(){return wave;}
        void wave(int value){wave=value;} public BlockPos activeChest(){return activeChest;} void activeChest(BlockPos value){activeChest=value;}
        BlockState chestReplacement(){return chestReplacement;} void chestReplacement(BlockState value){chestReplacement=value;}
        int partySize(){return partySize;}
    }
}
