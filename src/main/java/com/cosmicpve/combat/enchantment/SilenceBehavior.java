package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.adventure.AdventureRules;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.EnchantmentSuppressionService;
import com.cosmicpve.registry.ModEnchantments;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Parent-hit proc that leases suppression of one distinct equipped armor enchantment type. */
public final class SilenceBehavior implements ProcCandidateResolver {
    public static final Identifier SOURCE_ID = CosmicPVE.id("silence");
    public static final int DURATION_TICKS = 60;
    private final EnchantmentSuppressionService suppression;

    public SilenceBehavior(EnchantmentSuppressionService suppression) {
        this.suppression = suppression;
    }

    public static double chance(int level) {
        return level <= 0 ? 0.0 : (10.0 + Math.min(4, level)) / 100.0;
    }

    public static boolean eligibleWeapon(AttackCategory category, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return category == AttackCategory.MELEE
                ? stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)
                : category == AttackCategory.PROJECTILE && (LongbowBehavior.isBow(stack)
                        || stack.getItem() instanceof CrossbowItem);
    }

    public static boolean armorEnchantment(Identifier id) {
        return CosmicEnchantmentSpecs.find(id).map(spec -> switch (spec.equipmentApplicability()) {
            case "any_armor", "helmet", "chestplate", "leggings", "boots",
                    "helmet_or_chestplate", "chestplate_or_leggings", "boots_or_chestplate",
                    "boots_or_leggings" -> true;
            default -> false;
        }).orElse(false);
    }

    public static boolean validParent(com.cosmicpve.combat.api.CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY
                && context.parentSequenceId().isEmpty()
                && context.recursionPolicy() == RecursionPolicy.NORMAL
                && eligibleWeapon(context.category(), context.weaponSnapshot().stack());
    }

    public List<Identifier> eligibleTypes(LivingEntity target, long currentTick) {
        var distinct = new LinkedHashSet<Identifier>();
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(target.getItemBySlot(slot)).entrySet()) {
                entry.getKey().unwrapKey().ifPresent(key -> {
                    Identifier id = key.identifier();
                    if (entry.getIntValue() > 0 && armorEnchantment(id)
                            && (!AdventureRules.restricted(target) || AdventureRules.allows(id))
                            && !suppression.isSuppressedExceptSource(target, id, SOURCE_ID, currentTick)) {
                        distinct.add(id);
                    }
                });
            }
        }
        return distinct.stream().sorted(java.util.Comparator.comparing(Identifier::toString)).toList();
    }

    void activate(ProcEvent event) {
        if (event.target() == null) return;
        List<Identifier> eligible = eligibleTypes(event.target(), event.serverTick());
        if (eligible.isEmpty()) return;
        int index = Math.min(eligible.size() - 1, (int) (event.random().nextDouble() * eligible.size()));
        suppression.suppressEnchantment(event.target(), SOURCE_ID, eligible.get(index),
                DURATION_TICKS, event.serverTick());
    }

    @Override
    public List<ProcCandidate> resolve(ProcEvent event) {
        int level = event.effectiveEnchantments().level(ModEnchantments.SILENCE.identifier());
        if (event.hook() != ProcHook.ON_VALID_HIT || level <= 0 || event.attacker() == null
                || event.target() == null || event.combatResult().filter(result -> {
                    var context = result.context();
                    return result.isCommittedDamagingHit() && validParent(context);
                }).isEmpty()) return List.of();
        return List.of(new ProcCandidate(ModEnchantments.SILENCE.identifier(), ProcHook.ON_VALID_HIT,
                chance(level), Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(), Optional.of(CosmicPVE.id("silence_once")),
                ChildProcEligibility.ROOT_ONLY, ModEnchantments.SILENCE.identifier(),
                activation -> activate(activation.event()),
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.SILENCE.identifier())));
    }
}
