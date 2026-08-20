package com.cosmicpve.combat.proc;

import com.cosmicpve.combat.cooldown.CooldownScope;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Immutable, programmatic Step 4A proc definition. Later content may resolve into this type. */
public record ProcCandidate(
        Identifier effectId,
        ProcHook hook,
        double baseProbability,
        Optional<Identifier> cooldownKey,
        long baseCooldownTicks,
        CooldownScope cooldownScope,
        Optional<Identifier> cooldownScopeId,
        List<Double> cooldownDurationMultipliers,
        List<ProcCondition> conditions,
        Optional<Identifier> oncePerEventKey,
        ChildProcEligibility childEligibility,
        Identifier behaviorId,
        ProcAction action,
        ProcProvenance provenance) {
    public ProcCandidate {
        effectId = Objects.requireNonNull(effectId);
        hook = Objects.requireNonNull(hook);
        if (!Double.isFinite(baseProbability) || baseProbability < 0.0) {
            throw new IllegalArgumentException("Base proc probability must be finite and non-negative");
        }
        cooldownKey = cooldownKey == null ? Optional.empty() : cooldownKey;
        if (baseCooldownTicks < 0 || (baseCooldownTicks > 0) != cooldownKey.isPresent()) {
            throw new IllegalArgumentException("Cooldown key and positive duration must be supplied together");
        }
        cooldownScope = Objects.requireNonNull(cooldownScope);
        cooldownScopeId = cooldownScopeId == null ? Optional.empty() : cooldownScopeId;
        if (cooldownScope == CooldownScope.INSTANCE_SESSION && cooldownScopeId.isEmpty() && baseCooldownTicks > 0) {
            throw new IllegalArgumentException("Instance cooldowns require a scope ID");
        }
        if (cooldownScope != CooldownScope.INSTANCE_SESSION && cooldownScopeId.isPresent()) {
            throw new IllegalArgumentException("Only instance cooldowns may carry a scope ID");
        }
        cooldownDurationMultipliers = List.copyOf(cooldownDurationMultipliers);
        conditions = List.copyOf(conditions);
        oncePerEventKey = oncePerEventKey == null ? Optional.empty() : oncePerEventKey;
        childEligibility = Objects.requireNonNull(childEligibility);
        behaviorId = Objects.requireNonNull(behaviorId);
        action = Objects.requireNonNull(action);
        provenance = Objects.requireNonNull(provenance);
    }
}
