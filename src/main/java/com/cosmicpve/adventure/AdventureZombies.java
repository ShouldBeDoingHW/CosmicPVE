package com.cosmicpve.adventure;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import java.util.UUID;

public final class AdventureZombies {
    private AdventureZombies() {}
    public static int count(ServerPlayer player) {
        return player.level().getEntitiesOfClass(Zombie.class,player.getBoundingBox().inflate(64),z->z.getTags().contains("cosmic_adventure")).size();
    }
    public static boolean spawn(ServerPlayer player,AdventureSession session) {
        var level=(ServerLevel)player.level();if(count(player)>=8)return false;
        for(int attempt=0;attempt<12;attempt++) {
            double angle=player.getRandom().nextDouble()*Math.PI*2;int distance=24+player.getRandom().nextInt(25);
            int x=player.blockPosition().getX()+(int)(Math.cos(angle)*distance),z=player.blockPosition().getZ()+(int)(Math.sin(angle)*distance);
            // Scheduler never generates remote chunks just to spawn a mob.
            if(!level.hasChunk(x>>4,z>>4))continue;
            var p=AdventureSurface.nearby(level,x,z,0).orElse(null);if(p==null)continue;
            if(level.getNearestPlayer(p.getX()+.5,p.getY(),p.getZ()+.5,20,false)!=null)continue;
            var mob=EntityType.ZOMBIE.create(level,EntitySpawnReason.EVENT);if(mob==null)return false;
            mob.setBaby(false);mob.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);
            if(!level.noCollision(mob) || level.containsAnyLiquid(mob.getBoundingBox()))continue;
            mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.IRON_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));
            mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND_AXE));
            for(var slot:EquipmentSlot.VALUES)mob.setDropChance(slot,0F);
            mob.setCanPickUpLoot(false);mob.addTag("cosmic_adventure");mob.addTag(session.id().toString());
            return level.addFreshEntity(mob);
        }
        return false;
    }
    public static void cleanup(ServerLevel level,UUID session) {
        if(level!=null)for(var entity:level.getAllEntities())if(entity.getTags().contains("cosmic_adventure") && entity.getTags().contains(session.toString()))entity.discard();
    }
}
