package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Deterministic upper-hitbox projectile bonus using the real entity impact point. */
public final class SniperBehavior implements OutgoingDamageContributor {
    public static final double HEADSHOT_FRACTION = 0.80;
    private final ProjectileImpactContextService impacts;

    public SniperBehavior(ProjectileImpactContextService impacts) {
        this.impacts = impacts;
    }

    public static boolean isHeadshot(AABB box, Vec3 impact) {
        return isHeadshotAtFraction(box, impact, HEADSHOT_FRACTION);
    }

    public static boolean isHeadshotAtFraction(AABB box, Vec3 impact, double lowerFraction) {
        if (box == null || impact == null || box.getYsize() <= 0.0
                || impact.x < box.minX || impact.x > box.maxX
                || impact.y < box.minY || impact.y > box.maxY
                || impact.z < box.minZ || impact.z > box.maxZ) return false;
        return impact.y >= box.minY + box.getYsize() * lowerFraction;
    }

    public static double bonus(int level, boolean headshot) {
        return level > 0 && headshot ? 0.05 * Math.min(5, level) : 0.0;
    }

    public static boolean eligibleWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem);
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.category() != AttackCategory.PROJECTILE
                || context.target() == null || !(context.directSource() instanceof Projectile projectile)) return List.of();
        ItemStack launchWeapon = context.weaponSnapshot().stack();
        int level = context.effectiveEnchantments().level(ModEnchantments.SNIPER.identifier());
        if (level <= 0 || !eligibleWeapon(launchWeapon)) return List.of();
        boolean headshot = impacts.consume(projectile, context.target())
                .map(location -> isHeadshot(context.target().getBoundingBox(), location)).orElse(false);
        double value = bonus(level, headshot);
        return value > 0.0
                ? List.of(new OutgoingDamageContribution(ModEnchantments.SNIPER.identifier(), value))
                : List.of();
    }
}
