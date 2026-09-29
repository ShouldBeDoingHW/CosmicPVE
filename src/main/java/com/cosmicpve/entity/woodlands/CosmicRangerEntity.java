package com.cosmicpve.entity.woodlands;

import com.cosmicpve.adventure.ranger.CosmicRangerEquipment;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Persistent reusable-arena boss with sequential, encounter-owned jockey gates. */
public final class CosmicRangerEntity extends Bogged {
    public static final float SCALE=1.25F;
    public static final double MAX_HEALTH=350,ARMOR=3,TOUGHNESS=1,MOVEMENT_SPEED=.28;
    private final List<BlockPos> markers=new ArrayList<>();
    private final Set<UUID> waveMembers=new HashSet<>();
    private final Map<UUID,Vec3> participantSafePositions=new HashMap<>();
    private final Map<UUID,Vec3> waveSafePositions=new HashMap<>();
    private int phaseState;
    private long arenaId=Long.MIN_VALUE;
    private BoundingBox arenaBounds;
    private Vec3 rangerSafePosition;
    private long lastWaveFailureTick = -1000;

    public CosmicRangerEntity(EntityType<? extends Bogged> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder createAttributes(){return Bogged.createAttributes().add(Attributes.MAX_HEALTH,MAX_HEALTH)
            .add(Attributes.ARMOR,ARMOR).add(Attributes.ARMOR_TOUGHNESS,TOUGHNESS).add(Attributes.MOVEMENT_SPEED,MOVEMENT_SPEED);}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData data){
        var result=super.finalizeSpawn(level,difficulty,reason,data);setCustomName(Component.literal("Cosmic Ranger").withColor(0x43B03C));setCustomNameVisible(true);
        setPersistenceRequired();setCanPickUpLoot(false);getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(TOUGHNESS);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);setHealth((float)MAX_HEALTH);
        for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){
            setItemSlot(slot,CosmicRangerEquipment.armor(slot,level.registryAccess()));setDropChance(slot,0);}
        setItemSlot(EquipmentSlot.MAINHAND,CosmicRangerEquipment.bow(level.registryAccess()));setDropChance(EquipmentSlot.MAINHAND,0);
        reassessWeaponGoal();return result;
    }
    @Override public boolean doHurtTarget(ServerLevel level,net.minecraft.world.entity.Entity target){return false;}
    public void initializeArena(List<BlockPos> positions,long id){if(positions.size()!=4)throw new IllegalArgumentException("Exactly four markers required");markers.clear();markers.addAll(positions);arenaId=id;}
    public void initializeArena(List<BlockPos> positions,long id,BoundingBox bounds,ServerPlayer summoner){
        initializeArena(positions,id);arenaBounds=Objects.requireNonNull(bounds);rangerSafePosition=position();
        participantSafePositions.clear();participantSafePositions.put(summoner.getUUID(),summoner.position());
    }
    public long arenaId(){return arenaId;}
    public List<BlockPos> markers(){return List.copyOf(markers);} public int phaseState(){return phaseState;} public Set<UUID>waveMembers(){return Set.copyOf(waveMembers);}
    public Optional<BoundingBox> arenaBounds(){return Optional.ofNullable(arenaBounds);}
    public Set<UUID> participants(){return Set.copyOf(participantSafePositions.keySet());}
    public boolean phaseInvulnerable(){return phaseState==1||phaseState==3;}
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount){
        if(phaseInvulnerable()&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return false;
        float floor=healthFloor(source);
        boolean hit=amount>0&&super.hurtServer(level,source,amount);
        if(hit&&floor>0&&getHealth()<=floor+.01F)startWave(level,phaseState==0?1:2);
        return hit;
    }
    @Override protected void actuallyHurt(ServerLevel level,DamageSource source,float amount){
        float floor=healthFloor(source);super.actuallyHurt(level,source,amount);
        // Clamp after the complete vanilla/NeoForge damage pipeline so armor, Protection,
        // and modded reductions cannot prevent or accidentally skip a phase threshold.
        if(floor>0&&getHealth()<floor)setHealth(floor);
    }
    private float healthFloor(DamageSource source){
        if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return 0;
        return phaseState==0?(float)(MAX_HEALTH*.66):phaseState==2?(float)(MAX_HEALTH*.33):0;
    }
    private void startWave(ServerLevel level,int wave){
        waveMembers.clear();waveSafePositions.clear();
        var occupied = new ArrayList<AABB>();
        int pairs = 0;
        for(BlockPos preferred:waveSpawnPositions(level)){
            var mount=com.cosmicpve.registry.ModEntities.DREADMANE.get().create(level,EntitySpawnReason.TRIGGERED);if(mount==null)continue;
            var riderProbe=com.cosmicpve.registry.ModEntities.FOREST_FANATIC.get().create(level,EntitySpawnReason.JOCKEY);
            if(riderProbe==null)break;
            BlockPos spawn=findSafeWaveSpawn(level,preferred,mount,riderProbe.getBbHeight(),occupied);
            if(spawn==null)break;
            mount.snapTo(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,0,0);mount.finalizeSpawn(level,level.getCurrentDifficultyAt(spawn),EntitySpawnReason.TRIGGERED,null);
            mount.markEncounterOwner(this);
            // A rider cannot attach reliably until its vehicle has joined the level. Add the
            // pair explicitly so the encounter always owns and tracks both components.
            if(!level.addFreshEntity(mount))continue;
            var rider=mount.createJockeyRider(level,level.getCurrentDifficultyAt(spawn));
            if(rider==null||!level.addFreshEntity(rider)){if(rider!=null)rider.discard();mount.discard();continue;}
            waveMembers.add(mount.getUUID());waveMembers.add(rider.getUUID());
            waveSafePositions.put(mount.getUUID(),mount.position());waveSafePositions.put(rider.getUUID(),rider.position());
            occupied.add(pairVolume(mount,rider.getBbHeight())); pairs++;
        }
        if(pairs!=4){
            for(UUID id:waveMembers){var entity=level.getEntity(id);if(entity!=null)entity.discard();}
            waveMembers.clear();waveSafePositions.clear();
            if(level.getGameTime()-lastWaveFailureTick>100){
                com.cosmicpve.CosmicPVE.LOGGER.error("Cosmic Ranger wave {} could not safely place four jockey pairs at arena {}; phase remains retryable",wave,arenaId);
                lastWaveFailureTick=level.getGameTime();
            }
            return;
        }
        phaseState=wave==1?1:3;
    }
    private BlockPos findSafeWaveSpawn(ServerLevel level,BlockPos preferred,Entity mount,float riderHeight,List<AABB> occupied){
        for(BlockPos candidate:waveCandidates(preferred)){
            if(arenaBounds!=null && !arenaBounds.isInside(candidate))continue;
            BlockPos floor=candidate.below();
            if(!level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP))continue;
            mount.snapTo(candidate.getX()+.5,candidate.getY(),candidate.getZ()+.5,0,0);
            AABB volume=pairVolume(mount,riderHeight);
            if(arenaBounds!=null&&!contains(arenaBounds,volume))continue;
            if(occupied.stream().anyMatch(box->box.intersects(volume.inflate(.25))))continue;
            if(!level.noCollision(mount,volume))continue;
            return candidate;
        }
        return null;
    }
    private static AABB pairVolume(Entity mount,float riderHeight){
        AABB box=mount.getBoundingBox();
        return new AABB(box.minX,box.minY,box.minZ,box.maxX,box.maxY+riderHeight+.5,box.maxZ);
    }
    public static List<BlockPos> waveCandidates(BlockPos preferred){
        var result=new ArrayList<BlockPos>();
        for(int dy:new int[]{0,1,-1,2,-2,3,-3}){
            result.add(preferred.offset(0,dy,0));
            for(int radius=1;radius<=4;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                if(Math.max(Math.abs(dx),Math.abs(dz))==radius)result.add(preferred.offset(dx,dy,dz));
        }
        return result;
    }
    private List<BlockPos> waveSpawnPositions(ServerLevel level){
        if(!level.dimension().equals(com.cosmicpve.adventure.DenseWoodlandsSessionService.DIMENSION)){
            BlockPos center=blockPosition();
            return List.of(center.offset(0,0,-4),center.offset(4,0,0),center.offset(0,0,4),center.offset(-4,0,0));
        }
        return markers.stream().map(BlockPos::above).toList();
    }
    @Override public void tick(){
        if(!level().isClientSide()&&arenaBounds!=null)preventOutwardMovement();
        super.tick();if(!(level() instanceof ServerLevel level)||!isAlive()||isRemoved())return;
        enforceArena(level);
        if(phaseInvulnerable()){
            waveMembers.removeIf(id->{var e=level.getEntity(id);boolean gone=e==null||!e.isAlive()||e.isRemoved();if(gone)waveSafePositions.remove(id);return gone;});
            if(waveMembers.isEmpty())phaseState=phaseState==1?2:4;
        }
    }
    private void preventOutwardMovement(){
        Vec3 motion=getDeltaMovement();AABB box=getBoundingBox();
        double x=contains(arenaBounds,box.move(motion.x,0,0))?motion.x:0;
        double y=contains(arenaBounds,box.move(0,motion.y,0))?motion.y:0;
        double z=contains(arenaBounds,box.move(0,0,motion.z))?motion.z:0;
        if(x!=motion.x||y!=motion.y||z!=motion.z)setDeltaMovement(x,y,z);
    }
    private void enforceArena(ServerLevel level){
        if(arenaBounds==null)return;
        if(contains(arenaBounds,getBoundingBox()))rangerSafePosition=position();
        else restore(this,rangerSafePosition!=null?rangerSafePosition:fallbackPosition());

        for(ServerPlayer player:level.players()){
            var session=com.cosmicpve.adventure.DenseWoodlandsBootstrap.SESSIONS.session(player);
            boolean active=player.isAlive()&&session!=null&&session.phase()==com.cosmicpve.adventure.AdventureSession.Phase.ACTIVE
                    &&player.level()==level;
            if(!active){participantSafePositions.remove(player.getUUID());continue;}
            if(!participantSafePositions.containsKey(player.getUUID())&&contains(arenaBounds,player.getBoundingBox()))
                participantSafePositions.put(player.getUUID(),player.position());
            Vec3 safe=participantSafePositions.get(player.getUUID());if(safe==null)continue;
            if(contains(arenaBounds,player.getBoundingBox()))participantSafePositions.put(player.getUUID(),player.position());
            else restore(player,safe);
        }
        for(UUID id:List.copyOf(waveMembers)){
            Entity entity=level.getEntity(id);if(entity==null||!entity.isAlive()||entity.isRemoved()||entity.isPassenger())continue;
            if(contains(arenaBounds,entity.getBoundingBox()))waveSafePositions.put(id,entity.position());
            else restore(entity,waveSafePositions.getOrDefault(id,fallbackPosition()));
        }
    }
    public static boolean contains(BoundingBox bounds,AABB box){
        double epsilon=1.0E-4D;
        return box.minX>=bounds.minX()-epsilon&&box.maxX<=bounds.maxX()+1.0D+epsilon
                &&box.minY>=bounds.minY()-epsilon&&box.maxY<=bounds.maxY()+1.0D+epsilon
                &&box.minZ>=bounds.minZ()-epsilon&&box.maxZ<=bounds.maxZ()+1.0D+epsilon;
    }
    private Vec3 fallbackPosition(){
        if(!markers.isEmpty())return Vec3.atBottomCenterOf(markers.getFirst().above());
        return new Vec3((arenaBounds.minX()+arenaBounds.maxX()+1)*.5,arenaBounds.minY()+1,
                (arenaBounds.minZ()+arenaBounds.maxZ()+1)*.5);
    }
    private static void restore(Entity entity,Vec3 safe){
        if(entity instanceof ServerPlayer player&&player.level() instanceof ServerLevel level)
            player.teleportTo(level,safe.x,safe.y,safe.z,Set.of(),player.getYRot(),player.getXRot(),true);
        else entity.snapTo(safe.x,safe.y,safe.z,entity.getYRot(),entity.getXRot());
        entity.setDeltaMovement(Vec3.ZERO);entity.fallDistance=0;
        if(entity instanceof Mob mob)mob.getNavigation().stop();
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){
        var tables=new com.cosmicpve.reward.RewardTableService(com.cosmicpve.content.CosmicContent.repository(),new com.cosmicpve.reward.RewardGeneratorService());
        var context=new com.cosmicpve.reward.RewardGenerationContext(level.registryAccess(),getRandom(),
                source.getEntity() instanceof ServerPlayer p?p:null);
        int[][] directions={{0,-5},{5,0},{0,5},{-5,0}};
        for(int i=0;i<4;i++)for(var stack:tables.roll(com.cosmicpve.adventure.ranger.AdvancedWoodlandsRewards.TABLE,1,context)){
            var item=new net.minecraft.world.entity.item.ItemEntity(level,getX()+directions[i][0],getY()+1,getZ()+directions[i][1],stack);
            item.setDefaultPickUpDelay();level.addFreshEntity(item);
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("CosmicRangerPhase",phaseState);out.putLong("CosmicRangerArena",arenaId);out.putInt("ArenaMarkerCount",markers.size());
        for(int i=0;i<markers.size();i++)out.putLong("ArenaMarker"+i,markers.get(i).asLong());out.putInt("WaveMemberCount",waveMembers.size());int i=0;for(UUID id:waveMembers)out.putIntArray("WaveMember"+i++,net.minecraft.core.UUIDUtil.uuidToIntArray(id));
        if(arenaBounds!=null){out.putBoolean("HasArenaBounds",true);out.putInt("ArenaMinX",arenaBounds.minX());out.putInt("ArenaMinY",arenaBounds.minY());out.putInt("ArenaMinZ",arenaBounds.minZ());out.putInt("ArenaMaxX",arenaBounds.maxX());out.putInt("ArenaMaxY",arenaBounds.maxY());out.putInt("ArenaMaxZ",arenaBounds.maxZ());}
        writePosition(out,"RangerSafe",rangerSafePosition);writePositions(out,"Participant",participantSafePositions);writePositions(out,"WaveSafe",waveSafePositions);}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);phaseState=in.getIntOr("CosmicRangerPhase",0);arenaId=in.getLongOr("CosmicRangerArena",Long.MIN_VALUE);markers.clear();
        for(int i=0;i<in.getIntOr("ArenaMarkerCount",0);i++)markers.add(BlockPos.of(in.getLongOr("ArenaMarker"+i,0)));waveMembers.clear();
        for(int i=0;i<in.getIntOr("WaveMemberCount",0);i++)in.getIntArray("WaveMember"+i).ifPresent(a->waveMembers.add(net.minecraft.core.UUIDUtil.uuidFromIntArray(a)));
        arenaBounds=in.getBooleanOr("HasArenaBounds",false)?new BoundingBox(in.getIntOr("ArenaMinX",0),in.getIntOr("ArenaMinY",0),in.getIntOr("ArenaMinZ",0),in.getIntOr("ArenaMaxX",0),in.getIntOr("ArenaMaxY",0),in.getIntOr("ArenaMaxZ",0)):null;
        rangerSafePosition=readPosition(in,"RangerSafe");readPositions(in,"Participant",participantSafePositions);readPositions(in,"WaveSafe",waveSafePositions);}
    private static void writePosition(ValueOutput out,String key,Vec3 position){if(position==null)return;out.putBoolean(key+"Present",true);out.putDouble(key+"X",position.x);out.putDouble(key+"Y",position.y);out.putDouble(key+"Z",position.z);}
    private static Vec3 readPosition(ValueInput in,String key){return in.getBooleanOr(key+"Present",false)?new Vec3(in.getDoubleOr(key+"X",0),in.getDoubleOr(key+"Y",0),in.getDoubleOr(key+"Z",0)):null;}
    private static void writePositions(ValueOutput out,String key,Map<UUID,Vec3> positions){out.putInt(key+"Count",positions.size());int i=0;for(var entry:positions.entrySet()){out.putIntArray(key+"Id"+i,net.minecraft.core.UUIDUtil.uuidToIntArray(entry.getKey()));writePosition(out,key+"Pos"+i,entry.getValue());i++;}}
    private static void readPositions(ValueInput in,String key,Map<UUID,Vec3> positions){positions.clear();for(int i=0;i<in.getIntOr(key+"Count",0);i++){int index=i;in.getIntArray(key+"Id"+i).ifPresent(a->{Vec3 position=readPosition(in,key+"Pos"+index);if(position!=null)positions.put(net.minecraft.core.UUIDUtil.uuidFromIntArray(a),position);});}}
}
