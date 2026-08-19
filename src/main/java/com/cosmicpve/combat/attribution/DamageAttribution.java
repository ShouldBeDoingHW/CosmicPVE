package com.cosmicpve.combat.attribution;

import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatFlag;
import com.cosmicpve.combat.api.WeaponSnapshot;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public record DamageAttribution(
        @Nullable Entity directSource,
        @Nullable Entity creditedSource,
        @Nullable LivingEntity attacker,
        Optional<UUID> playerId,
        AttackCategory category,
        Set<CombatFlag> flags,
        WeaponSnapshot weaponSnapshot) {
    public DamageAttribution {
        playerId = playerId == null ? Optional.empty() : playerId;
        flags = Set.copyOf(flags);
    }
}
