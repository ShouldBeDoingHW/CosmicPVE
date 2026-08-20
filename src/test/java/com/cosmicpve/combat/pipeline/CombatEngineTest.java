package com.cosmicpve.combat.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageBounds;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.cosmicpve.combat.api.WeaponSnapshot;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantments;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CombatEngineTest {
    private final CombatEngine engine = new CombatEngine();

    @Test
    void appliesDocumentedOrdinaryDamageStageOrder() {
        var request = new CombatCalculationRequest(
                10.0,
                0.5,
                List.of(2.0, 0.5),
                new DamageBounds(0.0, 12.0),
                List.of(0.8, 0.5),
                new DamageBounds(5.0, 100.0),
                List.of());

        var result = engine.calculate(context(), request);
        var breakdown = result.breakdown();

        assertEquals(15.0, breakdown.afterAdditiveOutgoing());
        assertEquals(1.0, breakdown.outgoingMultiplierProduct());
        assertEquals(12.0, breakdown.afterPreDefenseBounds());
        assertEquals(0.4, breakdown.incomingMultiplierProduct(), 0.000001);
        assertEquals(5.0, breakdown.finalOrdinaryDamage());
        assertFalse(result.isCommittedDamagingHit());
    }

    @Test
    void keepsTrueDamagePacketsSeparateFromOrdinaryMath() {
        var truePacket = new TrueDamagePacket(CosmicPVE.id("test_true"), 7.0, true, true, true);
        var request = new CombatCalculationRequest(
                10.0,
                0.0,
                List.of(),
                DamageBounds.UNBOUNDED,
                List.of(0.5),
                DamageBounds.UNBOUNDED,
                List.of(truePacket));

        var result = engine.calculate(context(), request);

        assertEquals(5.0, result.breakdown().finalOrdinaryDamage());
        assertEquals(7.0, result.totalQueuedTrueDamage());
        assertEquals(1, result.trueDamagePackets().size());
        assertFalse(result.commit(0.0).isCommittedDamagingHit());
        assertTrue(result.commit(3.0).isCommittedDamagingHit());
    }

    @Test
    void rejectsInvalidModifiers() {
        var invalidAdditive = new CombatCalculationRequest(
                10.0, -1.1, List.of(), DamageBounds.UNBOUNDED, List.of(), DamageBounds.UNBOUNDED, List.of());
        var invalidMultiplier = new CombatCalculationRequest(
                10.0, 0.0, List.of(-0.5), DamageBounds.UNBOUNDED, List.of(), DamageBounds.UNBOUNDED, List.of());

        assertThrows(IllegalArgumentException.class, () -> engine.calculate(context(), invalidAdditive));
        assertThrows(IllegalArgumentException.class, () -> engine.calculate(context(), invalidMultiplier));
    }

    private static CombatContext context() {
        return new CombatContext(
                null,
                null,
                null,
                null,
                Optional.empty(),
                null,
                AttackCategory.UNKNOWN,
                DamageChannel.ORDINARY,
                Set.of(),
                WeaponSnapshot.empty(),
                EffectiveEnchantments.EMPTY,
                1L,
                OptionalLong.empty(),
                RecursionPolicy.NORMAL);
    }
}
