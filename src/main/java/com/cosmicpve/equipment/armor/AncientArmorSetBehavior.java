package com.cosmicpve.equipment.armor;

/** Health-dependent values for the bespoke Ancient full-set behavior. */
public final class AncientArmorSetBehavior {
    public static final double NORMAL_OUTGOING = 0.05;
    public static final double LOW_HEALTH_OUTGOING = 0.10;
    public static final double NORMAL_INCOMING = 0.95;
    public static final double LOW_HEALTH_INCOMING = 0.90;
    public static final double NORMAL_KNOCKBACK_MULTIPLIER = 0.90;
    public static final double LOW_HEALTH_KNOCKBACK_MULTIPLIER = 0.80;

    private AncientArmorSetBehavior() {}

    public static boolean isLowHealth(double currentHealth, double maximumHealth) {
        return Double.isFinite(currentHealth) && Double.isFinite(maximumHealth)
                && maximumHealth > 0.0 && currentHealth < maximumHealth * 0.5;
    }

    public static double outgoing(double currentHealth, double maximumHealth) {
        return isLowHealth(currentHealth, maximumHealth) ? LOW_HEALTH_OUTGOING : NORMAL_OUTGOING;
    }

    public static double incoming(double currentHealth, double maximumHealth) {
        return isLowHealth(currentHealth, maximumHealth) ? LOW_HEALTH_INCOMING : NORMAL_INCOMING;
    }

    public static double knockback(double currentHealth, double maximumHealth) {
        return isLowHealth(currentHealth, maximumHealth)
                ? LOW_HEALTH_KNOCKBACK_MULTIPLIER : NORMAL_KNOCKBACK_MULTIPLIER;
    }
}
