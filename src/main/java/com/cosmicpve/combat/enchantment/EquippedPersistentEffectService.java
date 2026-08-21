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
    static final int LEASE_TICKS = 60;
    static final int REFRESH_AT = 20;
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
        apply(entity, EquipmentSlot.HEAD, ModEnchantments.GLOWING.identifier(), MobEffects.NIGHT_VISION);
        apply(entity, EquipmentSlot.LEGS, ModEnchantments.OBSIDIANSHIELD.identifier(), MobEffects.FIRE_RESISTANCE);
    }

    private void apply(LivingEntity entity, EquipmentSlot slot, net.minecraft.resources.Identifier id,
                       Holder<MobEffect> effect) {
        var stack = entity.getItemBySlot(slot);
        int level = enchantments.resolve(stack, virtualGrants.apply(stack)).level(id);
        var effects = owned.computeIfAbsent(entity, ignored -> new java.util.HashMap<>());
        boolean ours = effects.containsKey(effect);
        var current = entity.getEffect(effect);
        if (level > 0) {
            if (ours && !isManaged(current)) {
                effects.remove(effect); // an external source superseded our lease
                return;
            }
            if (!ours && current != null) return;
            if (!ours || current.getDuration() <= REFRESH_AT) {
                entity.addEffect(managed(effect));
                effects.put(effect, Boolean.TRUE);
            }
        } else if (ours) {
            if (isManaged(current)) entity.removeEffect(effect);
            effects.remove(effect);
        }
        if (effects.isEmpty()) owned.remove(entity);
    }

    static MobEffectInstance managed(Holder<MobEffect> effect) {
        return new MobEffectInstance(effect, LEASE_TICKS, 0, true, false, false);
    }

    static boolean isManaged(MobEffectInstance effect) {
        return effect != null && effect.getAmplifier() == 0 && effect.isAmbient()
                && !effect.isVisible() && !effect.showIcon() && effect.getDuration() <= LEASE_TICKS;
    }
}
