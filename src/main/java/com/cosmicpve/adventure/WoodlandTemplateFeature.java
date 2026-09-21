package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Places authored templates through the biome's ordinary configured/placed feature pipeline. */
public final class WoodlandTemplateFeature extends Feature<WoodlandTemplateFeature.Config> {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, CosmicPVE.MOD_ID);
    static { FEATURES.register("woodland_template", () -> new WoodlandTemplateFeature()); }
    public static void register(IEventBus bus) { FEATURES.register(bus); }
    public record Config(String family, int variants) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("family").forGetter(Config::family),
                Codec.intRange(1,64).fieldOf("variants").forGetter(Config::variants)).apply(i, Config::new));
    }
    public record Placement(String variant, BlockPos origin, Rotation rotation, int minSurface, int maxSurface) {}
    private static final Map<String, LongAdder> COUNTS = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> CAMP_DIAGNOSTICS = new ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentLinkedDeque<Placement> CAMPS = new java.util.concurrent.ConcurrentLinkedDeque<>();
    public static List<Placement> recentCamps() { return List.copyOf(CAMPS); }
    public static Map<String,Long> campsiteDiagnostics() {
        var result=new TreeMap<String,Long>(); CAMP_DIAGNOSTICS.forEach((k,v)->result.put(k,v.sum()));return result;
    }
    private static boolean rejected(boolean camp,String reason) {
        if(camp) CAMP_DIAGNOSTICS.computeIfAbsent(reason,k->new LongAdder()).increment();return false;
    }
    private static final java.util.concurrent.ConcurrentLinkedDeque<Placement> RECENT = new java.util.concurrent.ConcurrentLinkedDeque<>();
    public static Map<String,Long> counts() {
        var result = new TreeMap<String,Long>(); COUNTS.forEach((k,v) -> result.put(k,v.sum())); return result;
    }
    public static List<Placement> recent() { return List.copyOf(RECENT); }
    public WoodlandTemplateFeature() { super(Config.CODEC); }
    @Override public boolean place(FeaturePlaceContext<Config> context) {
        var world = context.level(); var random = context.random(); var config = context.config();
        String variant = "woodlands_" + config.family() + (1 + random.nextInt(config.variants()));
        return placeTemplate(context, variant, Rotation.getRandom(random));
    }
    static boolean placeTemplate(FeaturePlaceContext<Config> context, String variant, Rotation rotation) {
        var world = context.level(); var random = context.random(); var config = context.config();
        boolean camp = config.family().equals("campsite");
        if(camp) CAMP_DIAGNOSTICS.computeIfAbsent("attempts",k->new LongAdder()).increment();
        Identifier id = CosmicPVE.id("woodlands/" + variant);
        var template = world.getLevel().getStructureManager().get(id).orElseThrow(() -> new IllegalStateException("Missing " + id));
        var settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).setKnownShape(true);
        var raw = template.save(new CompoundTag());
        var palette = raw.getListOrEmpty("palette");
        var blocks = new ArrayList<Cell>();
        for (var value : raw.getListOrEmpty("blocks")) {
            var tag = (CompoundTag)value; var p = tag.getListOrEmpty("pos");
            BlockState state = NbtUtils.readBlockState(world.registryAccess().lookupOrThrow(Registries.BLOCK),
                    palette.getCompoundOrEmpty(tag.getIntOr("state",0)));
            if (state.isAir() || state.is(Blocks.STRUCTURE_VOID)) continue;
            BlockPos pos = StructureTemplate.calculateRelativePosition(settings,
                    new BlockPos(p.getIntOr(0,0), p.getIntOr(1,0), p.getIntOr(2,0)));
            blocks.add(new Cell(pos, state));
        }
        int x = context.origin().getX(), z = context.origin().getZ();
        var contacts = blocks.stream().filter(c -> c.pos().getY()==0 && !c.state().getCollisionShape(world,context.origin()).isEmpty()).toList();
        // Shelves project one column beyond the authored soil. Include low props in the
        // integration footprint so their uphill soil can be cut under the same slope cap.
        if(camp) contacts=blocks.stream().filter(c->c.pos().getY()<=2).map(c->c.pos().atY(0)).distinct()
                .map(p->new Cell(p,Blocks.DIRT.defaultBlockState())).toList();
        if (contacts.isEmpty()) return rejected(camp,"no_contacts");
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (var c : contacts) {
            int h = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x+c.pos().getX(),z+c.pos().getZ());
            var floor = world.getBlockState(new BlockPos(x+c.pos().getX(),h-1,z+c.pos().getZ()));
            if (!floor.is(BlockTags.DIRT)) return rejected(camp,"not_soil");
            min=Math.min(min,h); max=Math.max(max,h);
        }
        if (max-min>2 || min<30 || max>131) return rejected(camp,"slope_or_height");
        // All three audited campsites have authored soil at local Y=0 and props starting at Y=1.
        // A highest-contact anchor lifted downhill contacts into pedestals. Seat soil at the
        // lowest surface instead. Remove at most two uphill soil layers inside the authored
        // floor/low-prop footprint; otherwise the corrected anchor rejects almost every rolling site.
        BlockPos origin = new BlockPos(x,camp ? campsiteOriginY(min) : max,z);
        if (isTreeFamily(config.family())
                && intersectsArenaFootprint(world, template.getBoundingBox(settings, origin))) return false;
        var shallowCut = new HashSet<BlockPos>();
        if(camp) for(var contact:contacts) {
            var floor=origin.offset(contact.pos());
            for(int y=1;y<=max-min;y++) {
                var at=floor.above(y);var existing=world.getBlockState(at);
                if(existing.is(BlockTags.DIRT) || existing.canBeReplaced() && existing.getFluidState().isEmpty())shallowCut.add(at);
                else if(!existing.isAir())return rejected(true,"clearance_obstruction");
            }
        }
        for (var c : blocks) {
            var at = origin.offset(c.pos()); var existing=world.getBlockState(at);
            if (camp && c.pos().getY()==0 && existing.is(BlockTags.DIRT)) continue;
            if(shallowCut.contains(at))continue;
            if(!existing.getFluidState().isEmpty())return rejected(camp,"fluid");
            if (!existing.isAir() && !existing.canBeReplaced()
                    && !(existing.is(BlockTags.LEAVES) && c.state().is(BlockTags.LEAVES))) return rejected(camp,"collision");
        }
        // Later woodland features must not build their support fills on campsite props.
        // Validate every support before writing any blocks (a boulder could otherwise bury a barrel).
        if(!camp) for(var contact:contacts) {
            var at=origin.offset(contact.pos()).below();boolean fill=false;
            for(int depth=0;depth<3 && world.getBlockState(at).isAir();depth++,at=at.below())fill=true;
            if(fill && !world.getBlockState(at).is(BlockTags.DIRT))return false;
        }
        for(var at:shallowCut)world.setBlock(at,Blocks.AIR.defaultBlockState(),2);
        // Bounded support fills (at most two blocks) keep wide ground contacts from floating.
        for (var c : camp ? List.<Cell>of() : contacts) {
            var at = origin.offset(c.pos()).below();
            for (int depth=0;depth<3 && world.getBlockState(at).isAir();depth++,at=at.below())
                world.setBlock(at,Blocks.DIRT.defaultBlockState(),2);
        }
        if (!template.placeInWorld(world,origin,origin,settings,random,2)) return false;
        COUNTS.computeIfAbsent(variant,k -> new LongAdder()).increment();
        var placed=new Placement(variant,origin,settings.getRotation(),min,max);
        RECENT.addLast(placed); while(RECENT.size()>4096) RECENT.pollFirst();
        if(camp) {
            CAMP_DIAGNOSTICS.computeIfAbsent("placed",k->new LongAdder()).increment();
            CAMPS.addLast(placed);while(CAMPS.size()>512)CAMPS.pollFirst();
        }
        return true;
    }
    static boolean isTreeFamily(String family) {
        return family.equals("small_tree")
                || family.equals("tall_tree")
                || family.equals("fallen_tree");
    }
    static boolean horizontallyIntersects(BoundingBox first, BoundingBox second) {
        return first.maxX() >= second.minX() && first.minX() <= second.maxX()
                && first.maxZ() >= second.minZ() && first.minZ() <= second.maxZ();
    }
    private static boolean intersectsArenaFootprint(net.minecraft.world.level.WorldGenLevel world,
            BoundingBox candidate) {
        var structure = world.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getValue(com.cosmicpve.adventure.ranger.WoodlandsArenaService.STRUCTURE);
        if (structure == null) return false;
        var manager = world instanceof net.minecraft.server.level.WorldGenRegion region
                ? world.getLevel().structureManager().forWorldGenRegion(region)
                : world.getLevel().structureManager();
        var visited = new HashSet<net.minecraft.world.level.levelgen.structure.StructureStart>();
        for (int chunkX = Math.floorDiv(candidate.minX(), 16); chunkX <= Math.floorDiv(candidate.maxX(), 16); chunkX++) {
            for (int chunkZ = Math.floorDiv(candidate.minZ(), 16); chunkZ <= Math.floorDiv(candidate.maxZ(), 16); chunkZ++) {
                for (var start : manager.startsForStructure(new net.minecraft.world.level.ChunkPos(chunkX, chunkZ), structure::equals)) {
                    if (visited.add(start) && horizontallyIntersects(candidate, start.getBoundingBox())) return true;
                }
            }
        }
        return false;
    }
    private record Cell(BlockPos pos, BlockState state) {}
    public static int campsiteOriginY(int lowestSurfaceAirY) { return lowestSurfaceAirY - 1; }
}
