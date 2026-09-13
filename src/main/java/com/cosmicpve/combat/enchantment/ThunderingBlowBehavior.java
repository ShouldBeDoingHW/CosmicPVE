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
import net.minecraft.tags.ItemTags;

public final class ThunderingBlowBehavior implements ProcCandidateResolver {
    private final ThunderingBlowVelocityService velocity;
    public ThunderingBlowBehavior(ThunderingBlowVelocityService velocity) { this.velocity = velocity; }
    public static double chance(int level) { return Math.max(0, Math.min(3, level)) * .03; }
    @Override public List<ProcCandidate> resolve(ProcEvent event) {
        int level = event.effectiveEnchantments().level(ModEnchantments.THUNDERING_BLOW.identifier());
        if (event.hook() != ProcHook.ON_VALID_HIT || level <= 0 || event.target() == null
                || event.combatResult().filter(result -> {
                    var context = result.context();
                    return context.channel() == DamageChannel.ORDINARY && context.category() == AttackCategory.MELEE
                            && context.parentSequenceId().isEmpty() && context.recursionPolicy() == RecursionPolicy.NORMAL
                            && context.weaponSnapshot().stack().is(ItemTags.SWORDS);
                }).isEmpty()) return List.of();
        return List.of(new ProcCandidate(ModEnchantments.THUNDERING_BLOW.identifier(), ProcHook.ON_VALID_HIT,
                chance(level), Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                List.of(), List.of(), Optional.of(CosmicPVE.id("thundering_blow_once")),
                ChildProcEligibility.ROOT_ONLY, ModEnchantments.THUNDERING_BLOW.identifier(),
                activation -> velocity.schedule(activation.event().target(), (int) activation.event().serverTick()),
                new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.THUNDERING_BLOW.identifier())));
    }
}
