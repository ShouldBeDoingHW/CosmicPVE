package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.proc.ProcEvent;
import com.cosmicpve.combat.stack.CombatStackService;
import com.cosmicpve.combat.stack.StackApplication;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class TrapBehavior {
    public static final Identifier STACK_ID = CosmicPVE.id("trap");
    public static final double BASE_CHANCE = 0.03;
    public static final double TITAN_BASE_CHANCE = 0.04;
    public static final int TITAN_DURATION_TICKS = 15;
    private TrapBehavior() {}
    public static double chance(int level) { return level <= 0 ? 0.0 : BASE_CHANCE; }
    public static double titanChance(int level) { return level <= 0 ? 0.0 : TITAN_BASE_CHANCE; }
    public static int durationTicks(int level) { return 4 * Math.max(1, Math.min(3, level)); }
    public static int nonShorteningDuration(int requested, long remaining) {
        return (int) Math.max(requested, Math.min(Integer.MAX_VALUE, remaining));
    }

    public static void activate(ProcEvent event, int durationTicks,
            CombatStackService stacks, SnareRootService roots) {
        if (event.target() == null || event.target().isDeadOrDying()) return;
        long tick = event.serverTick();
        long remaining = stacks.activeStacks(event.target(), tick).stream()
                .filter(stack -> stack.definitionId().equals(STACK_ID))
                .flatMap(stack -> stack.instances().stream())
                .mapToLong(stack -> stack.expirationTick() - tick).max().orElse(0L);
        int effectiveDuration = nonShorteningDuration(durationTicks, remaining);
        var result = stacks.addStack(event.target(), STACK_ID, 1,
                StackApplication.ephemeral(Optional.ofNullable(event.attacker()).map(e -> e.getUUID()),
                        event.tracePlayerId()), tick, effectiveDuration);
        if (result.finalCount() > 0) roots.applyTrap(event.target());
    }
}
