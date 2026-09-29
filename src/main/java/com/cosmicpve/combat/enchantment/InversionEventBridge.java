package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.action.CombatDeliveryScope;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcEventService;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import com.cosmicpve.combat.event.CombatEventBridge;

/** Dodge-ordered, pre-commit Inversion cancellation and virtual-offense dispatch. */
public final class InversionEventBridge {
    private final ProcEventService procs;
    private final EffectiveEnchantmentsResolver enchantments;
    private final CombatEventBridge combat;
    public InversionEventBridge(ProcEventService procs, EffectiveEnchantmentsResolver enchantments, CombatEventBridge combat) {
        this.procs = procs; this.enchantments = enchantments; this.combat = combat;
    }
    public static double chance(int level) { return Math.max(0, Math.min(4, level)) * .01; }
    public void onTargeted(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0 || event.getEntity().level().isClientSide() || CombatDeliveryScope.current().isPresent()) return;
        if (combat.defensiveCosmicSuppressed(event.getContainer())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)
                || event.getSource().getDirectEntity() != attacker) return;
        var sword = event.getEntity().getMainHandItem();
        if (!sword.is(ItemTags.SWORDS)) return;
        int level = enchantments.resolve(event.getEntity(), sword, List.of()).level(ModEnchantments.INVERSION.identifier());
        if (level <= 0) return;
        var weapon = event.getSource().getWeaponItem();
        if (weapon == null || weapon.isEmpty()) weapon = attacker.getWeaponItem();
        var capturedWeapon = weapon.copy();
        double parentDamage = combat.provisional(event.getContainer())
                .map(result -> result.breakdown().finalOrdinaryDamage()).orElse((double) event.getNewDamage());
        var activated = new boolean[1];
        var candidate = new ProcCandidate(ModEnchantments.INVERSION.identifier(), ProcHook.ON_TARGETED,
                chance(level), Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(), Optional.of(CosmicPVE.id("inversion_once")),
                ChildProcEligibility.ROOT_ONLY, Set.of(), ModEnchantments.INVERSION.identifier(), activation -> {
                    activated[0] = true;
                    procs.dispatchInvertedOffense(event.getEntity(), attacker, capturedWeapon,
                            parentDamage, event.getSource());
                }, new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.INVERSION.identifier()));
        procs.dispatchTargetedCandidate(event.getEntity(), attacker, event.getEntity(), candidate);
        if (activated[0]) event.setNewDamage(0.0F);
    }
}
