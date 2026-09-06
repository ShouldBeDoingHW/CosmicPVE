package com.cosmicpve.combat.enchantment;

import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEquipmentService;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;

public final class UndeadRuseBehavior {
    public static final int LIFETIME_TICKS = 900;
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, java.util.Set<java.util.UUID>> ACTIVE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private UndeadRuseBehavior() {}

    public static int equippedLevelHighest(LivingEntity entity) {
        int highest = 0;
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            highest = Math.max(highest, EnchantmentLevels.onStack(entity, entity.getItemBySlot(slot), ModEnchantments.UNDEAD_RUSE));
        return Math.min(10, highest);
    }
    public static double chance(int level) { return Math.max(0, Math.min(10, level)) * .005; }
    public static int cap(int level) { return level >= 10 ? 3 : level >= 5 ? 2 : level >= 1 ? 1 : 0; }

    public static int activeCount(LivingEntity owner) {
        var ids = ACTIVE.get(owner.getUUID());
        if (ids == null || ids.isEmpty()) return 0;
        var server = owner.level().getServer();
        if (server == null) return 0;
        ids.removeIf(id -> {
            for (var level : server.getAllLevels()) {
                var entity = level.getEntity(id);
                if (entity instanceof UndeadCorpseEntity corpse && !corpse.isRemoved() && !corpse.isDeadOrDying()) return false;
            }
            return true;
        });
        if (ids.isEmpty()) ACTIVE.remove(owner.getUUID(), ids);
        return ids.size();
    }

    public static void track(UndeadCorpseEntity corpse) {
        corpse.summonedOwnerId().ifPresent(owner -> ACTIVE.computeIfAbsent(owner,
                ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(corpse.getUUID()));
    }

    public static boolean summon(LivingEntity owner, int level) {
        if (!(owner.level() instanceof ServerLevel server) || activeCount(owner) >= cap(level)) return false;
        var corpse = ModEntities.UNDEAD_CORPSE.get().create(server, EntitySpawnReason.TRIGGERED);
        if (corpse == null) return false;
        corpse.snapTo(owner.getX() + 1.0, owner.getY(), owner.getZ(), owner.getYRot(), 0);
        UndeadCorpseEquipmentService.equipBase(corpse, server.registryAccess(), owner.getRandom());
        corpse.markSummonedAlly(owner, server.getGameTime() + LIFETIME_TICKS);
        server.addFreshEntity(corpse);
        track(corpse);
        return true;
    }
}
