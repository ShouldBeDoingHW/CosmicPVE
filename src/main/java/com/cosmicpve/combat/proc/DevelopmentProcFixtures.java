package com.cosmicpve.combat.proc;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.cooldown.CooldownScope;
import java.util.List;
import java.util.Optional;

/** Command/test-only fixtures. They are not registered as survival content or candidate sources. */
public final class DevelopmentProcFixtures {
    public static final net.minecraft.resources.Identifier GUARANTEED_ID = CosmicPVE.id("development/guaranteed_hit");
    public static final net.minecraft.resources.Identifier GUARANTEED_COOLDOWN =
            CosmicPVE.id("development/guaranteed_hit_cooldown");

    private DevelopmentProcFixtures() {}

    public static ProcCandidate guaranteedHit() {
        return new ProcCandidate(
                GUARANTEED_ID, ProcHook.ON_VALID_HIT, 1.0, Optional.of(GUARANTEED_COOLDOWN), 100,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.of(CosmicPVE.id("development/once_per_hit")),
                ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                CosmicPVE.id("development/noop"), ignored -> {},
                new ProcProvenance(ProcSourceKind.DEVELOPMENT, CosmicPVE.id("development/commands")));
    }

    public static ProcCandidate neverHit() {
        return new ProcCandidate(
                CosmicPVE.id("development/never_hit"), ProcHook.ON_VALID_HIT, 0.0,
                Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(),
                Optional.empty(), ChildProcEligibility.LIMITED_OFFENSIVE_REROLL,
                CosmicPVE.id("development/noop"), ignored -> {},
                new ProcProvenance(ProcSourceKind.DEVELOPMENT, CosmicPVE.id("development/commands")));
    }
}
