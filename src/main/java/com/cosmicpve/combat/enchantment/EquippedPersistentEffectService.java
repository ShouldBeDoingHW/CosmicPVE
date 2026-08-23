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
    static final int OBSIDIANSHIELD_LEASE_TICKS = 60;
    static final int GLOWING_REFRESH_AT = 300;
    static final int DEFAULT_REFRESH_AT = 20;
    private final EffectiveEnchantmentsResolver enchantments;
    private final Function<net.minecraft.world.item.ItemStack, List<VirtualEnchantmentGrant>> virtualGrants;
    private final Map<LivingEntity, Map<Holder<MobEffect>, Boolean>> owned =
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
        apply(entity, EquipmentSlot.LEGS, ModEnchantments.OBSIDIANSHIELD.identifier(),
                MobEffects.FIRE_RESISTANCE, OBSIDIANSHIELD_LEASE_TICKS, DEFAULT_REFRESH_AT);
    }

    private void apply(LivingEntity entity, EquipmentSlot slot, net.minecraft.resources.Identifier id,
                       Holder<MobEffect> effect, int leaseTicks, int refreshAt) {
        var stack = entity.getItemBySlot(slot);
        int level = enchantments.resolve(stack, virtualGrants.apply(stack)).level(id);
        var effects = owned.computeIfAbsent(entity, ignored -> new java.util.HashMap<>());
        boolean ours = effects.containsKey(effect);
        var current = entity.getEffect(effect);
        if (level > 0) {
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
