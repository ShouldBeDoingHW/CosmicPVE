package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcCandidateResolver;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.equipment.mask.MaskResolver;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One Luck-modified roll containing both Dodge and Turkey's flat contribution. */
public final class DodgeProcResolver implements ProcCandidateResolver {
    public static final net.minecraft.resources.Identifier CANDIDATE = CosmicPVE.id("dodge_decision");
    private final MaskResolver masks;
    public DodgeProcResolver(MaskResolver masks) { this.masks = masks; }
    public static double chance(int level, boolean turkey) {
        return Math.min(1, Math.max(0, Math.min(5, level)) * .005 + (turkey ? .02 : 0));
    }
    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_TARGETED || event.target() == null || event.attacker() == null) return List.of();
        int level = event.effectiveEnchantments().level(ModEnchantments.DODGE.identifier());
        boolean turkey = masks.resolve(event.target()).stream().anyMatch(mask -> mask.behavior() == MaskBehavior.TURKEY);
        double chance = chance(level, turkey);
        if (chance <= 0) return List.of();
        return List.of(new ProcCandidate(CANDIDATE, ProcHook.ON_TARGETED, chance, Optional.empty(), 0,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.of(CANDIDATE),
                ChildProcEligibility.LIMITED_DEFENSIVE_REACTION, Set.of(), CANDIDATE, activation -> {},
                new ProcProvenance(level > 0 ? ProcSourceKind.ACTUAL_ENCHANTMENT : ProcSourceKind.OTHER,
                        level > 0 ? ModEnchantments.DODGE.identifier() : CosmicPVE.id("turkey_mask"))));
    }
}
