package com.cosmicpve.combat.enchantment;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.DamageChannel;
import com.cosmicpve.combat.memory.RecentCombatMemoryService;
import com.cosmicpve.combat.pipeline.OutgoingDamageContribution;
import com.cosmicpve.combat.pipeline.OutgoingDamageContributor;
import com.cosmicpve.registry.ModEnchantments;
import java.util.List;

/** Deterministic level-scaled bonus against a target that recently damaged this attacker three times. */
public final class RageBehavior implements OutgoingDamageContributor {
    public static final int REQUIRED_HITS = 3;
    private final RecentCombatMemoryService memory;

    public RageBehavior(RecentCombatMemoryService memory) {
        this.memory = memory;
    }

    public static long windowTicks(int level) {
        return level <= 0 ? 0L : (4L + level) * 20L;
    }

    public static boolean active(int level, int recentHits) {
        return level > 0 && recentHits >= REQUIRED_HITS;
    }

    public static double bonus(int level) {
        return level <= 0 ? 0.0 : (5.0 + level) / 100.0;
    }

    @Override
    public List<OutgoingDamageContribution> resolve(CombatContext context) {
        if (context.channel() != DamageChannel.ORDINARY || context.attacker() == null || context.target() == null) {
            return List.of();
        }
        int level = context.effectiveEnchantments().level(ModEnchantments.RAGE.identifier());
        if (level <= 0 || context.attacker().level().getServer() == null) return List.of();
        long tick = context.attacker().level().getServer().getTickCount();
        int hits = memory.count(context.target().getUUID(), context.attacker().getUUID(), tick, windowTicks(level));
        return active(level, hits)
                ? List.of(new OutgoingDamageContribution(ModEnchantments.RAGE.identifier(), bonus(level)))
                : List.of();
    }
}
