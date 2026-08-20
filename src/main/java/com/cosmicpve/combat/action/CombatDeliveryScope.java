package com.cosmicpve.combat.action;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.TrueDamagePacket;
import java.util.Optional;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/** Links a centralized child delivery to the NeoForge damage callbacks on the same server thread. */
public final class CombatDeliveryScope {
    private static final ThreadLocal<Deque<State>> ACTIVE = ThreadLocal.withInitial(ArrayDeque::new);

    private CombatDeliveryScope() {}

    public static <T> T call(CombatContext context, TrueDamagePacket truePacket, Supplier<T> action) {
        return call(context, truePacket, false, action);
    }

    public static <T> T call(
            CombatContext context, TrueDamagePacket truePacket, boolean doublestrikeBypass, Supplier<T> action) {
        Deque<State> stack = ACTIVE.get();
        if (!stack.isEmpty() && context.parentSequenceId().stream().noneMatch(id -> id == stack.peek().context().attackSequenceId())) {
            throw new IllegalStateException("Nested combat delivery must be linked to the active parent sequence");
        }
        stack.push(new State(context, Optional.ofNullable(truePacket), doublestrikeBypass));
        try {
            return action.get();
        } finally {
            stack.pop();
            if (stack.isEmpty()) {
                ACTIVE.remove();
            }
        }
    }

    public static Optional<State> current() {
        Deque<State> stack = ACTIVE.get();
        if (stack.isEmpty()) {
            ACTIVE.remove();
            return Optional.empty();
        }
        return Optional.of(stack.peek());
    }

    public record State(CombatContext context, Optional<TrueDamagePacket> truePacket, boolean doublestrikeBypass) {}
}
