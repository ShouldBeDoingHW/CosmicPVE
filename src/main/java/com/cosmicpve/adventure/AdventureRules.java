package com.cosmicpve.adventure;

import com.cosmicpve.equipment.enchantment.*;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** Effect policy only: never edits carried gear or its presentation components. */
public final class AdventureRules {
    private AdventureRules() {}
    public static boolean restricted(LivingEntity entity) {
        if (!(entity instanceof net.minecraft.server.level.ServerPlayer player) || entity.level()==null
                || !entity.level().dimension().equals(DenseWoodlandsSessionService.DIMENSION)) return false;
        var session = DenseWoodlandsBootstrap.SESSIONS.session(player);
        return session != null && session.phase() == AdventureSession.Phase.ACTIVE;
    }
    public static boolean allows(Identifier id) {
        return CosmicEnchantmentSpecs.find(id).map(s -> switch(s.tier()) {
            case SIMPLE, UNIQUE, ELITE -> true; default -> false;
        }).orElse(true);
    }
    public static boolean allowsArmorSet(Identifier id) { return ArmorSetIds.RANGER.equals(id); }
    public static EffectiveEnchantments filter(LivingEntity owner,EffectiveEnchantments input) {
        if(!restricted(owner))return input;
        var result=new java.util.HashMap<Identifier,EffectiveEnchantment>();
        input.entries().stream().filter(e -> allows(e.id())).forEach(e -> result.put(e.id(),e));
        return new EffectiveEnchantments(result);
    }
}
