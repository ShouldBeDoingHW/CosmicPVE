package com.cosmicpve.equipment.mask;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.pipeline.IncomingDamageContribution;
import com.cosmicpve.combat.pipeline.IncomingDamageContributor;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.content.definition.stack.StackPolarity;
import com.cosmicpve.combat.stack.CombatStackService;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.HolyWhiteScrollService;

public final class MaskCombatResolver implements OutgoingDamageContributor, IncomingDamageContributor {
    /** Legacy diagnostic ID retained for compatibility; Turkey now contributes to Dodge's single roll. */
    public static final net.minecraft.resources.Identifier TURKEY_DODGE = CosmicPVE.id("turkey_mask_dodge");
    private final MaskResolver masks;
    private final com.cosmicpve.activity.ActivityContextService activities;
    private final CombatStackService stacks;
    public MaskCombatResolver(MaskResolver masks) {
        this(masks, new com.cosmicpve.activity.ActivityContextService(),
                new CombatStackService(com.cosmicpve.content.CosmicContent.repository()));
    }
    public MaskCombatResolver(MaskResolver masks, com.cosmicpve.activity.ActivityContextService activities) {
        this(masks, activities, new CombatStackService(com.cosmicpve.content.CosmicContent.repository()));
    }
    public MaskCombatResolver(MaskResolver masks, com.cosmicpve.activity.ActivityContextService activities,
            CombatStackService stacks) {
        this.masks = masks;
        this.activities = activities;
        this.stacks = stacks;
    }

    @Override public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.attacker() == null || context.channel() != DamageChannel.ORDINARY) return List.of();
        var equippedMasks = masks.resolve(context.attacker());
        double bonus = equippedMasks.stream().mapToDouble(definition -> switch (definition.behavior()) {
            case PURGE -> .03; case PARTY -> .01; case DRAGON -> .02;
            case BANDIT -> activities.isAdventure(context.attacker()) ? .08 : 0.0;
            default -> 0.0;
        }).sum();
        if (equippedMasks.stream().anyMatch(definition -> definition.behavior() == MaskBehavior.THANOS)
                && targetHasActualMastery(context.target())) bonus += .06;
        if (equippedMasks.stream().anyMatch(definition -> definition.behavior() == MaskBehavior.MONOPOLY))
            bonus += monopolyOutgoingBonus(holyGearCount(context.attacker()));
        if (equippedMasks.stream().anyMatch(definition -> definition.behavior() == MaskBehavior.TIKI)
                && hasNegativeStack(context.attacker())) bonus += .03;
        return bonus == 0.0 ? List.of() : List.of(new OutgoingDamageContribution(CosmicPVE.id("mask_loadout"), bonus));
    }

    @Override public List<IncomingDamageContribution> resolveIncoming(CombatContext context) {
        if (context.target() == null) return List.of();
        var equipped = masks.resolve(context.target());
        if (context.damageSource() != null) {
            if (equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.ZEUS)
                    && context.damageSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT))
                return List.of(new IncomingDamageContribution(CosmicPVE.id("zeus_mask_lightning_immunity"), 0.0));
        }
        if (context.channel() != DamageChannel.ORDINARY) return List.of();
        var result = new java.util.ArrayList<IncomingDamageContribution>();
        if (equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.PARTY))
            result.add(new IncomingDamageContribution(CosmicPVE.id("party_mask"), .99));
        if (equipped.stream().anyMatch(d -> d.behavior() == MaskBehavior.TIKI) && hasNegativeStack(context.target()))
            result.add(new IncomingDamageContribution(CosmicPVE.id("tiki_mask"), .97));
        return List.copyOf(result);
    }

    private boolean hasNegativeStack(net.minecraft.world.entity.LivingEntity entity) {
        var server = entity.level().getServer();
        return server != null && !stacks.byPolarity(entity, StackPolarity.NEGATIVE, server.getTickCount()).isEmpty();
    }

    static int holyGearCount(net.minecraft.world.entity.LivingEntity entity) {
        return holyGearCount(entity.getMainHandItem(), List.of(
                entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST),
                entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET)));
    }

    static int holyGearCount(ItemStack mainHand, Iterable<ItemStack> armor) {
        int count = HolyWhiteScrollService.isHoly(mainHand) ? 1 : 0;
        for (ItemStack stack : armor) if (HolyWhiteScrollService.isHoly(stack)) count++;
        return count;
    }

    static double monopolyOutgoingBonus(int holyGearPieces) {
        if (holyGearPieces < 0 || holyGearPieces > 5) throw new IllegalArgumentException("Holy gear count must be 0..5");
        return holyGearPieces * .01;
    }

    static boolean anyActualMastery(Iterable<ItemStack> gear) {
        for (ItemStack stack : gear) if (hasActualMastery(stack)) return true;
        return false;
    }

    static List<ItemStack> currentGear(net.minecraft.world.entity.LivingEntity entity) {
        var gear = new java.util.ArrayList<ItemStack>(5);
        gear.add(entity.getMainHandItem());
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) gear.add(entity.getItemBySlot(slot));
        return List.copyOf(gear);
    }

    static boolean targetHasActualMastery(net.minecraft.world.entity.LivingEntity target) {
        if (target == null) return false;
        return anyActualMastery(currentGear(target));
    }

    static boolean hasActualMastery(ItemStack stack) {
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            var key = entry.getKey().unwrapKey();
            if (key.isPresent() && CosmicEnchantmentSpecs.find(key.orElseThrow().identifier())
                    .map(spec -> spec.tier() == CosmicEnchantmentTier.MASTERY).orElse(false)) return true;
        }
        return false;
    }

}
