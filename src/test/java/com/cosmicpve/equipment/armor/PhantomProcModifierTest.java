package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.cooldown.*;
import com.cosmicpve.combat.proc.*;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.*;
import org.junit.jupiter.api.Test;

class PhantomProcModifierTest {
    @Test void onlyClassifiedMasteryCandidatesReceiveRelativeMultiplier() {
        var engine = new ProcEngine(new CooldownService(), new ProcTraceService());
        var event = new ProcEvent(ProcHook.ON_VALID_HIT, 1, OptionalLong.empty(), RecursionPolicy.NORMAL,
                UUID.randomUUID(), Optional.empty(), 0, List.of(1.0), Map.of(ArmorSetIds.MASTERY_PROC, 1.25),
                List.of(1.0), Set.of(), EffectiveEnchantments.EMPTY, Optional.empty(), null, null, () -> .99);
        var result = engine.evaluate(event, List.of(candidate("mastery", Set.of(ArmorSetIds.MASTERY_PROC)),
                candidate("ordinary", Set.of())));
        assertEquals(.25, result.evaluations().get(0).finalChance(), 1e-9);
        assertEquals(.20, result.evaluations().get(1).finalChance(), 1e-9);
    }

    private static ProcCandidate candidate(String id, Set<net.minecraft.resources.Identifier> classifications) {
        return new ProcCandidate(CosmicPVE.id("test/" + id), ProcHook.ON_VALID_HIT, .20, Optional.empty(), 0,
                CooldownScope.EPHEMERAL_COMBAT, Optional.empty(), List.of(), List.of(), Optional.empty(),
                ChildProcEligibility.ROOT_ONLY, classifications, CosmicPVE.id("test/noop"), ignored -> {},
                new ProcProvenance(ProcSourceKind.DEVELOPMENT, CosmicPVE.id("test")));
    }
}
