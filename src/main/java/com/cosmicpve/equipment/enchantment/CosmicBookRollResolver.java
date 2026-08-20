package com.cosmicpve.equipment.enchantment;

import java.util.OptionalInt;
import java.util.function.IntSupplier;

public final class CosmicBookRollResolver {
    public enum Outcome { SUCCESS, FAILED_SURVIVED, FAILED_DESTRUCTIVE }
    public record Decision(Outcome outcome, int successRoll, OptionalInt destroyRoll) {}
    private CosmicBookRollResolver() {}
    public static Decision resolve(int successRate, int destroyRate, IntSupplier randomRoll) {
        int success = successRate == 100 ? 1 : checked(randomRoll.getAsInt());
        if (success <= successRate) return new Decision(Outcome.SUCCESS, success, OptionalInt.empty());
        int destroy = destroyRate == 100 ? 1 : checked(randomRoll.getAsInt());
        return new Decision(destroy <= destroyRate ? Outcome.FAILED_DESTRUCTIVE : Outcome.FAILED_SURVIVED,
                success, OptionalInt.of(destroy));
    }
    private static int checked(int value) {
        if (value < 1 || value > 100) throw new IllegalStateException("Roll must be in [1,100]");
        return value;
    }
}
