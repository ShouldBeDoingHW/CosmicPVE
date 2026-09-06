package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.ChildCombatActionService;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.proc.ProcActivation;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.equipment.mask.MaskResolver;
import com.cosmicpve.registry.ModEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class StormcallerBehavior {
    private StormcallerBehavior() {}
    public static int aggregateLevels(int chest, int boots) { return Math.min(10, Math.max(0, chest) + Math.max(0, boots)); }
    public static int equippedLevelTotal(LivingEntity entity) { return aggregateLevels(
            EnchantmentLevels.onStack(entity, entity.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.STORMCALLER),
            EnchantmentLevels.onStack(entity, entity.getItemBySlot(EquipmentSlot.FEET), ModEnchantments.STORMCALLER)); }
    public static double chance(int total) { return Math.min(.05, Math.max(0, total) * .01); }
    public static double trueDamage(int total) { return 1.0 + .2 * Math.max(0, Math.min(10, total)); }
    public static int durationTicks(int total) { return total >= 8 ? 60 : 40; }
    public static boolean immune(LivingEntity entity) { return new MaskResolver(CosmicContent.repository()).resolve(entity)
            .stream().anyMatch(mask -> mask.behavior() == MaskBehavior.ZEUS); }
    public static void activate(ProcActivation activation, int total, ChildCombatActionService actions) {
        var event = activation.event(); var attacker = event.attacker(); var parent = event.combatResult().orElse(null);
        if (attacker == null || parent == null || attacker.isDeadOrDying() || immune(attacker)) return;
        if (attacker.level() instanceof ServerLevel level) {
            var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) { bolt.setVisualOnly(true); bolt.setPos(attacker.getX(), attacker.getY(), attacker.getZ()); level.addFreshEntity(bolt); }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, durationTicks(total), 0));
        actions.deliverTrue(parent.context(), attacker,
                TrueDamagePacket.standard(CosmicPVE.id("stormcaller"), trueDamage(total)), RecursionPolicy.NO_PROCS);
    }
}
