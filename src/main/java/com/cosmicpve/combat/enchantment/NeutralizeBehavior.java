package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
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
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tags.ItemTags;

public final class NeutralizeBehavior implements ProcCandidateResolver {
    public static double chance(int level) { return Math.max(0, Math.min(5, level)) * .01; }
    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        if (event.hook() != ProcHook.ON_PRE_DEFENSE) return List.of();
        int level = event.effectiveEnchantments().level(ModEnchantments.NEUTRALIZE.identifier());
        if (level <= 0 || event.combatResult().filter(result -> {
            var context = result.context();
            return context.channel() == DamageChannel.ORDINARY && context.category() == AttackCategory.MELEE
                    && context.parentSequenceId().isEmpty() && context.recursionPolicy() == RecursionPolicy.NORMAL
                    && context.weaponSnapshot().stack().is(ItemTags.AXES);
        }).isEmpty()) return List.of();
        return List.of(new ProcCandidate(ModEnchantments.NEUTRALIZE.identifier(), ProcHook.ON_PRE_DEFENSE,
                chance(level), Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(), Optional.of(CosmicPVE.id("neutralize_once")), ChildProcEligibility.ROOT_ONLY,
                Set.of(), ModEnchantments.NEUTRALIZE.identifier(), activation -> {},
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.NEUTRALIZE.identifier())));
    }
}
