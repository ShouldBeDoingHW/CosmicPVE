package com.cosmicpve.combat.action;

import com.cosmicpve.combat.api.CombatContext;
import com.cosmicpve.combat.api.TrueDamagePacket;
import java.util.Optional;
import java.util.function.Supplier;

/** Links a centralized child delivery to the NeoForge damage callbacks on the same server thread. */
public final class CombatDeliveryScope {
    private static final ThreadLocal<State> ACTIVE = new ThreadLocal<>();

    private CombatDeliveryScope() {}

    public static <T> T call(CombatContext context, TrueDamagePacket truePacket, Supplier<T> action) {
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("Nested raw combat delivery is not allowed; construct another child action instead");
        }
        ACTIVE.set(new State(context, Optional.ofNullable(truePacket)));
        try {
            return action.get();
        } finally {
            ACTIVE.remove();
        }
    }

    public static Optional<State> current() {
        return Optional.ofNullable(ACTIVE.get());
    }

    public record State(CombatContext context, Optional<TrueDamagePacket> truePacket) {}
}
