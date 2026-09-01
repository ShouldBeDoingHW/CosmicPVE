package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.ownership.OwnedAllyResolver;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEngine;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.registry.ModEnchantments;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** One aggregate defensive proc that heals an owned ally and arms one ordinary parent attack. */
public final class SpiritLinkBehavior implements ProcCandidateResolver, OutgoingDamageContributor {
    public static final double BASE_CHANCE = 0.05;
    public static final double HEAL_PER_LEVEL = 0.5;
    public static final double ATTACK_BONUS_PER_LEVEL = 0.01;
    public static final int MAX_AGGREGATE_LEVEL = 14;
    public static final net.minecraft.resources.Identifier ONCE_KEY = CosmicPVE.id("spirit_link_once_per_damage");

    private final Map<UUID, Charge> charges = new HashMap<>();
    private final Map<Long, PendingConsumption> pendingConsumptions = new HashMap<>();

    public static int aggregateLevels(int helmet, int chestplate) {
        return Math.min(MAX_AGGREGATE_LEVEL, Math.max(0, helmet) + Math.max(0, chestplate));
    }

    public static int equippedLevel(LivingEntity wearer) {
        return aggregateLevels(
                EnchantmentLevels.onStack(wearer.getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.SPIRIT_LINK),
                EnchantmentLevels.onStack(wearer.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.SPIRIT_LINK));
    }

    public static float healing(int aggregateLevel) {
        return (float) (HEAL_PER_LEVEL * Math.max(0, Math.min(MAX_AGGREGATE_LEVEL, aggregateLevel)));
    }

    public static double attackBonus(int aggregateLevel) {
        return ATTACK_BONUS_PER_LEVEL * Math.max(0, Math.min(MAX_AGGREGATE_LEVEL, aggregateLevel));
    }

    public static List<OutgoingDamageContribution> contribution(
            DamageChannel channel, AttackCategory category, boolean parentAttack, double bonus) {
        return channel == DamageChannel.ORDINARY && parentAttack && bonus > 0.0
                && (category == AttackCategory.MELEE || category == AttackCategory.PROJECTILE)
                ? List.of(new OutgoingDamageContribution(ModEnchantments.SPIRIT_LINK.identifier(), bonus))
                : List.of();
    }

    public static int selectedAllyIndex(double roll, int size) {
        if (!(roll >= 0.0 && roll < 1.0) || size <= 0) throw new IllegalArgumentException("Invalid ally selection");
        return Math.min(size - 1, (int) Math.floor(roll * size));
    }

    public boolean hasCharge(UUID wearer) { return charges.containsKey(wearer); }
    public double storedBonus(UUID wearer) { return Optional.ofNullable(charges.get(wearer)).map(Charge::bonus).orElse(0.0); }

    @Override
    public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() == ProcHook.ON_DAMAGE_TAKEN) return incomingCandidate(event);
        if (event.hook() == ProcHook.ON_VALID_HIT) return consumptionCandidate(event);
        return List.of();
    }

    private List<ProcCandidate> incomingCandidate(ProcEvent event) {
        LivingEntity wearer = event.target();
        if (wearer == null) return List.of();
        int aggregate = equippedLevel(wearer);
        if (aggregate <= 0) return List.of();
        var allies = OwnedAllyResolver.livingAllies(wearer);
        if (allies.isEmpty()) return List.of();
        return List.of(new ProcCandidate(
                ModEnchantments.SPIRIT_LINK.identifier(), ProcHook.ON_DAMAGE_TAKEN, BASE_CHANCE,
                Optional.empty(), 0L, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.of(ONCE_KEY), ChildProcEligibility.LIMITED_DEFENSIVE_REACTION, Set.of(),
                ModEnchantments.SPIRIT_LINK.identifier(), activation -> {
                    var currentAllies = OwnedAllyResolver.livingAllies(wearer);
                    if (currentAllies.isEmpty()) return;
                    LivingEntity selected = currentAllies.get(selectedAllyIndex(
                            activation.event().random().nextDouble(), currentAllies.size()));
                    selected.heal(healing(aggregate));
                    charges.put(wearer.getUUID(), new Charge(attackBonus(aggregate)));
                }, new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, CosmicPVE.id("equipped_armor"))));
    }

    private List<ProcCandidate> consumptionCandidate(ProcEvent event) {
        PendingConsumption pending = pendingConsumptions.remove(event.sequenceId());
        if (pending == null || event.attacker() == null || !pending.wearer().equals(event.attacker().getUUID()))
            return List.of();
        return List.of(new ProcCandidate(
                ModEnchantments.SPIRIT_LINK.identifier(), ProcHook.ON_VALID_HIT, 1.0,
                Optional.empty(), 0L, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.empty(), ChildProcEligibility.ROOT_ONLY, Set.of(ProcEngine.DETERMINISTIC_CLASSIFICATION),
                ModEnchantments.SPIRIT_LINK.identifier(), activation ->
                        charges.remove(pending.wearer(), pending.charge()),
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.SPIRIT_LINK.identifier())));
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.parentSequenceId().isPresent()
                || context.attacker() == null || context.target() == null
                || (context.category() != AttackCategory.MELEE && context.category() != AttackCategory.PROJECTILE)) {
            return List.of();
        }
        Charge charge = charges.get(context.attacker().getUUID());
        if (charge == null) return List.of();
        if (pendingConsumptions.size() > 1024) pendingConsumptions.clear();
        pendingConsumptions.put(context.attackSequenceId(), new PendingConsumption(context.attacker().getUUID(), charge));
        return contribution(context.channel(), context.category(), context.parentSequenceId().isEmpty(), charge.bonus());
    }

    public void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        UUID entity = event.getEntity().getUUID();
        charges.remove(entity);
        pendingConsumptions.entrySet().removeIf(entry -> entry.getValue().wearer().equals(entity));
    }

    void storeForTest(UUID wearer, int aggregateLevel) {
        charges.put(wearer, new Charge(attackBonus(aggregateLevel)));
    }
    void consumeForTest(UUID wearer) { charges.remove(wearer); }

    private record Charge(double bonus) {}
    private record PendingConsumption(UUID wearer, Charge charge) {}
}
