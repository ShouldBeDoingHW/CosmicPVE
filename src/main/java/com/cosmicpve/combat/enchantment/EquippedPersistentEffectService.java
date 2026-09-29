package com.cosmicpve.combat.enchantment;

import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import com.cosmicpve.equipment.enchantment.VirtualEnchantmentGrant;
import java.util.List;
import java.util.function.Function;

/** Owns short hidden leases only when CosmicPVE created the effect. */
public final class EquippedPersistentEffectService {
    static final int GLOWING_LEASE_TICKS = 600;
    static final int GLOWING_REFRESH_AT = 300;
    static final int DEFAULT_REFRESH_AT = 20;
    private final EffectiveEnchantmentsResolver enchantments;
    private final Function<net.minecraft.world.item.ItemStack, List<VirtualEnchantmentGrant>> virtualGrants;
    private final Map<LivingEntity, Map<Holder<MobEffect>, Boolean>> owned =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<LivingEntity, ImplantSchedule> implants =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<LivingEntity, ImplantSchedule> alienImplants =
            Collections.synchronizedMap(new WeakHashMap<>());

    public EquippedPersistentEffectService(EffectiveEnchantmentsResolver enchantments) {
        this(enchantments, ignored -> List.of());
    }

    public EquippedPersistentEffectService(EffectiveEnchantmentsResolver enchantments,
            Function<net.minecraft.world.item.ItemStack, List<VirtualEnchantmentGrant>> virtualGrants) {
        this.enchantments = enchantments;
        this.virtualGrants = virtualGrants;
    }

    public void tick(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        apply(entity, EquipmentSlot.HEAD, ModEnchantments.GLOWING.identifier(),
                MobEffects.NIGHT_VISION, GLOWING_LEASE_TICKS, GLOWING_REFRESH_AT);
        tickImplants(entity);
        tickAlienImplants(entity);
        reconcileCurseKnockback(entity);
        reconcileOverloadHealth(entity);
    }

    public void reconcileOverloadHealth(LivingEntity entity) {
        OverloadBehavior.reconcile(entity, OverloadBehavior.effectiveBonus(entity, enchantments, virtualGrants));
    }

    void reconcileCurseKnockback(LivingEntity entity) {
        var attribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
        if (attribute == null) return;
        double amount = CurseBehavior.state(entity).knockbackResistance();
        var existing = attribute.getModifier(CurseBehavior.KNOCKBACK_MODIFIER_ID);
        if (amount == 0.0) {
            if (existing != null) attribute.removeModifier(CurseBehavior.KNOCKBACK_MODIFIER_ID);
            return;
        }
        if (existing != null && Double.compare(existing.amount(), amount) == 0) return;
        if (existing != null) attribute.removeModifier(CurseBehavior.KNOCKBACK_MODIFIER_ID);
        attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                CurseBehavior.KNOCKBACK_MODIFIER_ID, amount,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
    }

    void tickImplants(LivingEntity entity) {
        var helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        int level = enchantments.resolve(entity, helmet, virtualGrants.apply(helmet))
                .level(ModEnchantments.IMPLANTS.identifier());
        if (level <= 0) { implants.remove(entity); return; }
        int now = entity.tickCount;
        int interval = ImplantsBehavior.intervalTicks(level);
        ImplantSchedule schedule = implants.get(entity);
        if (schedule == null || schedule.level() != level) {
            implants.put(entity, new ImplantSchedule(level, ImplantsBehavior.nextHealTick(now, level)));
            return;
        }
        if (now >= schedule.nextHealTick()) {
            if (entity.getHealth() < entity.getMaxHealth()) entity.heal(ImplantsBehavior.HEAL_AMOUNT);
            implants.put(entity, new ImplantSchedule(level, ImplantsBehavior.nextHealTick(now, level)));
        }
    }

    private record ImplantSchedule(int level, int nextHealTick) {}

    void tickAlienImplants(LivingEntity entity) {
        var helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        int level = enchantments.resolve(entity, helmet, virtualGrants.apply(helmet))
                .level(ModEnchantments.ALIEN_IMPLANTS.identifier());
        if (level <= 0) { alienImplants.remove(entity); return; }
        int now = entity.tickCount;
        ImplantSchedule schedule = alienImplants.get(entity);
        if (schedule == null || schedule.level() != level) {
            alienImplants.put(entity, new ImplantSchedule(level, now + AlienImplantsBehavior.intervalTicks(level)));
            return;
        }
        if (now >= schedule.nextHealTick()) {
            AlienImplantsBehavior.activate(entity);
            alienImplants.put(entity, new ImplantSchedule(level, now + AlienImplantsBehavior.intervalTicks(level)));
        }
    }

    private void apply(LivingEntity entity, EquipmentSlot slot, net.minecraft.resources.Identifier id,
                       Holder<MobEffect> effect, int leaseTicks, int refreshAt) {
        var stack = entity.getItemBySlot(slot);
        int level = enchantments.resolve(entity, stack, virtualGrants.apply(stack)).level(id);
        reconcileLease(entity, effect, level > 0, leaseTicks, refreshAt);
    }

    /** Reusable authoritative owned-effect seam for equipment whose activation is resolved elsewhere. */
    public void reconcileLease(LivingEntity entity, Holder<MobEffect> effect, boolean active,
            int leaseTicks, int refreshAt) {
        var effects = owned.computeIfAbsent(entity, ignored -> new java.util.HashMap<>());
        boolean ours = effects.containsKey(effect);
        var current = entity.getEffect(effect);
        if (active) {
            if (ours && !isManaged(current, leaseTicks)) {
                effects.remove(effect); // an external source superseded our lease
                return;
            }
            if (!ours && current != null) return;
            if (!ours || current.getDuration() <= refreshAt) {
                entity.addEffect(managed(effect, leaseTicks));
                effects.put(effect, Boolean.TRUE);
            }
        } else if (ours) {
            if (isManaged(current, leaseTicks)) entity.removeEffect(effect);
            effects.remove(effect);
        }
        if (effects.isEmpty()) owned.remove(entity);
    }

    static MobEffectInstance managed(Holder<MobEffect> effect, int leaseTicks) {
        return new MobEffectInstance(effect, leaseTicks, 0, true, false, false);
    }

    static boolean isManaged(MobEffectInstance effect, int leaseTicks) {
        return effect != null && effect.getAmplifier() == 0 && effect.isAmbient()
                && !effect.isVisible() && !effect.showIcon() && effect.getDuration() <= leaseTicks;
    }
}
