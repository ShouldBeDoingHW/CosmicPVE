package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/** Firing-Bow snapshot enchantment with a live target-hand condition at impact. */
public final class LongbowBehavior implements OutgoingDamageContributor {
    public static final TagKey<net.minecraft.world.item.Item> BOWS =
            TagKey.create(Registries.ITEM, CosmicPVE.id("enchantable/bow"));
    public static double bonus(int level) {
        return Math.max(0, Math.min(5, level)) * .02;
    }

    public static boolean isBow(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(BOWS) || stack.getItem() instanceof BowItem);
    }

    public static boolean targetHoldsBow(ItemStack mainHand, ItemStack offHand) {
        return isBow(mainHand) || isBow(offHand);
    }

    public static boolean validParentProjectile(CombatContext context) {
        return context.channel() == DamageChannel.ORDINARY
                && context.category() == AttackCategory.PROJECTILE
                && context.parentSequenceId().isEmpty()
                && context.recursionPolicy() == RecursionPolicy.NORMAL;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (!validParentProjectile(context) || context.target() == null
                || !isBow(context.weaponSnapshot().stack())
                || !targetHoldsBow(context.target().getMainHandItem(), context.target().getOffhandItem())) {
            return List.of();
        }
        double value = bonus(context.effectiveEnchantments().level(ModEnchantments.LONGBOW.identifier()));
        return value == 0.0 ? List.of()
                : List.of(new OutgoingDamageContribution(ModEnchantments.LONGBOW.identifier(), value));
    }
}
