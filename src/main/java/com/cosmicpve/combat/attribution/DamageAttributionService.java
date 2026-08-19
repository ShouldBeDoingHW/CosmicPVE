package com.cosmicpve.combat.attribution;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatFlag;
import com.cosmicpve.combat.api.WeaponSnapshot;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/** Central ownership resolution for player/mob melee and player/mob projectiles. */
public final class DamageAttributionService {
    public DamageAttribution resolve(DamageSource source) {
        Entity direct = source.getDirectEntity();
        Entity credited = source.getEntity();
        if (credited == null && direct instanceof Projectile projectile) {
            credited = projectile.getOwner();
        }

        LivingEntity attacker = credited instanceof LivingEntity living
                ? living
                : direct instanceof LivingEntity living ? living : null;
        var playerId = attacker instanceof Player player
                ? Optional.of(player.getUUID())
                : Optional.<java.util.UUID>empty();

        AttackCategory category;
        var flags = EnumSet.noneOf(CombatFlag.class);
        if (direct instanceof Projectile) {
            category = AttackCategory.PROJECTILE;
            flags.add(CombatFlag.PROJECTILE);
        } else if (attacker != null) {
            category = AttackCategory.MELEE;
            flags.add(CombatFlag.MELEE);
        } else if (direct == null && credited == null) {
            category = AttackCategory.ENVIRONMENTAL;
            flags.add(CombatFlag.ENVIRONMENTAL);
        } else {
            category = AttackCategory.UNKNOWN;
        }

        ItemStack weapon = source.getWeaponItem();
        return new DamageAttribution(
                direct,
                credited,
                attacker,
                playerId,
                category,
                flags,
                weapon == null ? WeaponSnapshot.empty() : new WeaponSnapshot(weapon));
    }
}
