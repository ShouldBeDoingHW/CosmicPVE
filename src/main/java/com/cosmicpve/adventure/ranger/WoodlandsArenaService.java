package com.cosmicpve.adventure.ranger;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.entity.woodlands.CosmicRangerEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Authorizes summons with the generated structure start, never with a loose lectern/block pattern. */
public final class WoodlandsArenaService {
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> STRUCTURE =
            net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, CosmicPVE.id("woodlands_arena"));
    private WoodlandsArenaService() {}
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand()!=InteractionHand.MAIN_HAND||!RangerTomb.isCanonical(event.getItemStack())
                ||!(event.getLevel() instanceof ServerLevel level)||!(event.getEntity() instanceof ServerPlayer player)
                ||!(level.getBlockEntity(event.getPos()) instanceof LecternBlockEntity))return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        var structure=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(STRUCTURE);
        if(structure==null){reject(player,"This arena is unavailable.");return;}
        var start=level.structureManager().getStructureWithPieceAt(event.getPos(),structure);
        if(!start.isValid()){reject(player,"The Tomb can only be used in a generated Woodlands Arena.");return;}
        var box=start.getBoundingBox();long arenaId=start.getChunkPos().toLong();
        boolean active=false;for(var entity:level.getAllEntities())if(entity instanceof CosmicRangerEntity ranger&&ranger.isAlive()&&ranger.arenaId()==arenaId){active=true;break;}
        if(active){
            reject(player,"This Woodlands Arena already has a living Cosmic Ranger.");return;}
        List<BlockPos> markers=findMarkers(level,box);
        if(markers.size()!=4){CosmicPVE.LOGGER.error("Woodlands Arena at {} has {} Light markers; expected exactly four",box,markers.size());
            reject(player,"This Woodlands Arena is missing its four Ranger markers.");return;}
        var ranger=com.cosmicpve.registry.ModEntities.COSMIC_RANGER.get().create(level,EntitySpawnReason.TRIGGERED);
        if(ranger==null){reject(player,"The Cosmic Ranger could not be summoned.");return;}
        BlockPos spawn=event.getPos().above();ranger.snapTo(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,player.getYRot()+180,0);
        ranger.initializeArena(markers,arenaId,box,player);ranger.finalizeSpawn(level,level.getCurrentDifficultyAt(spawn),EntitySpawnReason.TRIGGERED,null);
        if(!level.addFreshEntity(ranger)){reject(player,"The Cosmic Ranger could not be summoned.");return;}
        event.getItemStack().shrink(1);
        ((LecternBlockEntity)level.getBlockEntity(event.getPos())).clearContent();
    }
    public static List<BlockPos> findMarkers(ServerLevel level,net.minecraft.world.level.levelgen.structure.BoundingBox box){
        var result=new ArrayList<BlockPos>();
        for(int y=box.minY();y<=box.maxY();y++)for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++){
            var p=new BlockPos(x,y,z);if(level.getBlockState(p).is(Blocks.LIGHT))result.add(p.immutable());}
        return List.copyOf(result);
    }
    private static void reject(ServerPlayer player,String text){player.displayClientMessage(Component.literal(text).withColor(0x43B03C),true);}
}
