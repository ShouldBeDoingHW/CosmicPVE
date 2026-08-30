package com.cosmicpve.equipment.mask;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MaskRuntimeEventBridge {
    public static final int LOVER_INTERVAL_TICKS = 100;
    public static final int SCARECROW_INTERVAL_TICKS = 160;
    public static final Identifier MAX_HEALTH_ID = CosmicPVE.id("mask_max_health");
    public static final Identifier MOVEMENT_ID = CosmicPVE.id("mask_movement_speed");
    private final MaskResolver masks;
    private final ProcEventService procs;
    private final EffectiveEnchantmentsResolver enchantments;
    private final Map<LivingEntity, Schedule> schedules = Collections.synchronizedMap(new WeakHashMap<>());
    public MaskRuntimeEventBridge(MaskResolver masks, ProcEventService procs, EffectiveEnchantmentsResolver enchantments) {
        this.masks = masks; this.procs = procs; this.enchantments = enchantments;
    }

    public void onPlayerTick(PlayerTickEvent.Post event) { tick(event.getEntity()); }
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)) tick(living);
    }
    private void tick(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        var equipped = masks.resolve(entity);
        double health = equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.SANTA) ? 2.0 : 0.0;
        reconcile(entity, Attributes.MAX_HEALTH, MAX_HEALTH_ID, health, AttributeModifier.Operation.ADD_VALUE);
        boolean lover = equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.LOVER);
        boolean scarecrow = equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.SCARECROW) && entity instanceof Player;
        if (!lover && !scarecrow) { schedules.remove(entity); return; }
        int now = entity.tickCount;
        Schedule schedule = schedules.computeIfAbsent(entity, ignored -> new Schedule(now + LOVER_INTERVAL_TICKS, now + SCARECROW_INTERVAL_TICKS));
        int nextHeal = schedule.nextHeal(); int nextFood = schedule.nextFood();
        if (lover && now >= nextHeal) { if (entity.getHealth() < entity.getMaxHealth()) entity.heal(1.0F); nextHeal = now + LOVER_INTERVAL_TICKS; }
        // Vanilla converts nutrition * modifier * 2 to saturation: 1 * .25 * 2 = .5.
        if (scarecrow && now >= nextFood) { ((Player) entity).getFoodData().eat(1, .25F); nextFood = now + SCARECROW_INTERVAL_TICKS; }
        schedules.put(entity, new Schedule(lover ? nextHeal : now + LOVER_INTERVAL_TICKS,
                scarecrow ? nextFood : now + SCARECROW_INTERVAL_TICKS));
    }

    public void onTargeted(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0 || event.getEntity().level().isClientSide()
                || (masks.resolve(event.getEntity()).stream().noneMatch(d -> d.behavior() == MaskBehavior.TURKEY)
                    && enchantments.resolve(event.getEntity(), java.util.List.of()).level(ModEnchantments.DODGE.identifier()) <= 0)) return;
        var scoped = com.cosmicpve.combat.action.CombatDeliveryScope.current();
        if (scoped.isPresent() && scoped.orElseThrow().context().channel()
                != com.cosmicpve.combat.api.DamageChannel.ORDINARY) return;
        var attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null) return;
        if (procs.dispatchRoot(ProcHook.ON_TARGETED, event.getEntity(), attacker, event.getEntity()).activationCount() > 0)
            event.setNewDamage(0.0F);
    }

    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().is(MobEffects.POISON)
                && masks.resolve(event.getEntity()).stream().anyMatch(d -> d.behavior() == MaskBehavior.DRAGON))
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    private static void reconcile(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            Identifier id, double amount, AttributeModifier.Operation operation) {
        var instance = entity.getAttribute(attribute);
        if (instance == null) return;
        var existing = instance.getModifier(id);
        if (amount == 0.0) { if (existing != null) instance.removeModifier(id); return; }
        if (existing == null || existing.amount() != amount || existing.operation() != operation)
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
    }
    private record Schedule(int nextHeal, int nextFood) {}
    static double movementBonus(MaskBehavior behavior) {
        return switch (behavior) { case REINDEER -> .05; case PARTY -> .01; default -> 0.0; };
    }
}
