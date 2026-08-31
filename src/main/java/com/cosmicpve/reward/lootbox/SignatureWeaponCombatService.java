package com.cosmicpve.reward.lootbox;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.equipment.armor.ArmorSetResolver;
import com.cosmicpve.registry.ModDataComponents;

/** Adds the signature weapon's flat point at the ordinary base-damage stage. */
public final class SignatureWeaponCombatService {
    public static final double MATCHING_SET_BONUS = 1.0;
    private final ArmorSetResolver armorSets;
    public SignatureWeaponCombatService(ArmorSetResolver armorSets) { this.armorSets = armorSets; }

    public double baseDamageBonus(CombatContext context) {
        if (context.attacker() == null) return 0.0;
        var identity = context.weaponSnapshot().stack().get(ModDataComponents.SIGNATURE_WEAPON.get());
        if (identity == null) return 0.0;
        var resolvedSet = armorSets.resolve(context.attacker()).map(value -> value.id());
        return matches(identity, context.category(), resolvedSet) ? MATCHING_SET_BONUS : 0.0;
    }
    public static boolean matches(SignatureWeaponIdentity identity, AttackCategory category,
            java.util.Optional<net.minecraft.resources.Identifier> resolvedSet) {
        boolean categoryMatches = identity.kind() == SignatureWeaponIdentity.Kind.MELEE
                ? category == AttackCategory.MELEE : category == AttackCategory.PROJECTILE;
        return categoryMatches && resolvedSet.filter(identity.matchingArmorSetId()::equals).isPresent();
    }
}
